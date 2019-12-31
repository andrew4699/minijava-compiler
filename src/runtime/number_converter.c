/*
 * Convert IEEE-754 32-bit/64-bit floats/doubles into decimal,
 * following the formatting and rounding rules from Java Double.toString().
 *
 * This code was taken from the Google Android Dalvik runtime system,
 * and modified by rrh@newrelic.com as needed to translate from Java to C,
 * and change the C portions as needed to not run within Dalvik.
 *
 * See:
 *   https://gitorious.org/0xdroid/dalvik/source/24f92a2538b58e541ef5b5b143e094c9ba16c035:libcore/luni/src/main/native/org_apache_harmony_luni_util_NumberConvert.c#L16-58
 *   https://gitorious.org/0xdroid/dalvik/source/24f92a2538b58e541ef5b5b143e094c9ba16c035:libcore/luni/src/main/java/org/apache/harmony/luni/util/NumberConverter.java#L26
 *
 *   http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.67.4438&rep=rep1&type=pdf
 */

#include <stdint.h>
#include <math.h>
#include <string.h>
#include <stdio.h>

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

typedef int jint;
typedef long long int jlong;
typedef int jboolean;
typedef double jdouble;
typedef float jfloat;

typedef int boolean;
#define true 1
#define false 0

static double invLogOfTenBaseTwo;
static int64_t TEN_TO_THE[20];
static int did_fill_TEN_TO_THE = 0;

static void fill_TEN_TO_THE() {
    unsigned i;

    invLogOfTenBaseTwo = log(2.0) / log(10.0);
    TEN_TO_THE[0] = 1LL;
    for (i = 1; i < sizeof(TEN_TO_THE)/sizeof(TEN_TO_THE[0]); ++i) {
        int64_t previous = TEN_TO_THE[i - 1];
        TEN_TO_THE[i] = (previous << 1LL) + (previous << 3LL);
    }
}

typedef struct _NumberConverter {
    int setCount; // number of times u and k have been gotten
    int getCount; // number of times u and k have been set
    int32_t uArray[64];
    int firstK;
} NumberConverter;

void initialize_NumberConverter(NumberConverter *ncp) {
  memset(ncp->uArray, 0, sizeof(ncp->uArray));
  ncp->setCount = 0;
  ncp->getCount = 0;
  ncp->firstK = 0;
  if (did_fill_TEN_TO_THE++ == 0) {  /* DANGER: NOT THREAD SAFE */
    fill_TEN_TO_THE();
  }
}

static void bigIntDigitGeneratorInstImpl(NumberConverter *ncp, jlong f, jint e,
      jboolean isDenormalized, jboolean mantissaIsZero, jint p);

static void longDigitGenerator(NumberConverter *ncp, int64_t f, int e, boolean isDenormalized,
      boolean mantissaIsZero, int p);
static void freeFormatExponential(NumberConverter *ncp, char *buf, int buf_len);
static void freeFormat(NumberConverter *ncp, char *buf, int buf_len);

static void convertD(NumberConverter *ncp, double inputNumber, char *buf, int buf_len) {
    int p = 1023 + 52; // the power offset (precision)
    int64_t signMask = 0x8000000000000000LL; // the mask to get the sign of
    // the number
    int64_t eMask = 0x7FF0000000000000LL; // the mask to get the power bits
    int64_t fMask = 0x000FFFFFFFFFFFFFLL; // the mask to get the significand
    // bits

    // int64_t inputNumberBits = doubleToLongBits(inputNumber);
    int64_t inputNumberBits = *(int64_t *)&inputNumber;

    // the value of the sign... 0 is positive, ~0 is negative
    // const char *signString = (inputNumberBits & signMask) == 0 ? "" : "-";
    boolean is_negative = 0;
    if (inputNumberBits & signMask) {
      is_negative = 1;
      *buf++ = '-';
      buf_len -= 1;
    }
    *buf = 0;

    // the value of the 'power bits' of the inputNumber
    int e = (int) ((inputNumberBits & eMask) >> 52);
    // the value of the 'significand bits' of the inputNumber
    int64_t f = inputNumberBits & fMask;
    boolean mantissaIsZero = f == 0;
    int pow = 0, numBits = 52;

    if (e == 2047) {
        if (mantissaIsZero) {
          strcpy(buf, "Infinity");
        } else {
          if (is_negative) {
            buf--; buf_len++;
          }
          strcpy(buf, "NaN");
        }
        return;
    }
    if (e == 0) {
        if (mantissaIsZero) {
            *buf++ = '0'; *buf++ = '.'; *buf++ = '0'; *buf = 0;
            return;
        }
        if (f == 1) {
            // special case to increase precision even though 2 *
            // Double.MIN_VALUE is 1.0e-323
            strcpy(buf, "4.9E-324");
            //return signString + "4.9E-324";
            return;
        }
        pow = 1 - p; // a denormalized number
        int64_t ff = f;
        while ((ff & 0x0010000000000000L) == 0) {
            ff = ff << 1;
            numBits--;
        }
    } else {
        // 0 < e < 2047
        // a "normalized" number
        f = f | 0x0010000000000000L;
        pow = e - p;
    }

    if ((-59 < pow && pow < 6) || (pow == -59 && !mantissaIsZero)) {
        longDigitGenerator(ncp, f, pow, e == 0, mantissaIsZero, numBits);
    } else {
        // bigIntDigitGenerator
        // Java_org_apache_harmony_luni_util_NumberConverter_bigIntDigitGeneratorInstImpl
        bigIntDigitGeneratorInstImpl(ncp, f, pow, e == 0, mantissaIsZero,
                numBits);
    }

    if (inputNumber >= 1e7D || inputNumber <= -1e7D
            || (inputNumber > -1e-3D && inputNumber < 1e-3D)) {
        freeFormatExponential(ncp, buf, buf_len);
        return;
    }
    
    freeFormat(ncp, buf, buf_len);
    return;
}

static void convertF(NumberConverter *ncp, float inputNumber, char *buf, int buf_len) {
    int p = 127 + 23; // the power offset (precision)
    int signMask = 0x80000000; // the mask to get the sign of the number
    int eMask = 0x7F800000; // the mask to get the power bits
    int fMask = 0x007FFFFF; // the mask to get the significand bits

    int inputNumberBits = *(int32_t *)&inputNumber;
    // the value of the sign... 0 is positive, ~0 is negative
    boolean is_negative = 0;
    if (inputNumberBits & signMask) {
      is_negative = 1;
      *buf++ = '-';
      buf_len -= 1;
    }
    *buf = 0;

    // the value of the 'power bits' of the inputNumber
    int e = (inputNumberBits & eMask) >> 23;
    // the value of the 'significand bits' of the inputNumber
    int f = inputNumberBits & fMask;
    boolean mantissaIsZero = f == 0;
    int pow = 0, numBits = 23;

    if (e == 255) {
        if (mantissaIsZero) {
          strcpy(buf, "Infinity");
        } else {
          if (is_negative) {
            buf--; buf_len++;
          }
          strcpy(buf, "NaN");
        }
        return;
    }
    if (e == 0) {
        if (mantissaIsZero) {
            *buf++ = '0'; *buf++ = '.'; *buf++ = '0'; *buf = 0;
            return;
        }
        pow = 1 - p; // a denormalized number
        if (f < 8) { // want more precision with smallest values
            f = f << 2;
            pow -= 2;
        }
        int ff = f;
        while ((ff & 0x00800000) == 0) {
            ff = ff << 1;
            numBits--;
        }
    } else {
        // 0 < e < 255
        // a "normalized" number
        f = f | 0x00800000;
        pow = e - p;
    }

    if ((-59 < pow && pow < 35) || (pow == -59 && !mantissaIsZero)) {
        longDigitGenerator(ncp, f, pow, e == 0, mantissaIsZero, numBits);
    } else {
        bigIntDigitGeneratorInstImpl(ncp, f, pow, e == 0, mantissaIsZero,
                numBits);
    }
    if (inputNumber >= 1e7f || inputNumber <= -1e7f
            || (inputNumber > -1e-3f && inputNumber < 1e-3f)) {
        freeFormatExponential(ncp, buf, buf_len);
        return;
    }
    
    freeFormat(ncp, buf, buf_len);
    return;
}

static void freeFormatExponential(NumberConverter *ncp, char *buf, int buf_len) {
    // corresponds to process "Free-Format Exponential"
    char formattedDecimal[25];
    formattedDecimal[0] = (char) ('0' + ncp->uArray[ncp->getCount++]);
    formattedDecimal[1] = '.';
    // the position the next character is to be inserted into
    // formattedDecimal
    int charPos = 2;

    int k = ncp->firstK;
    int expt = k;
    while (true) {
        k--;
        if (ncp->getCount >= ncp->setCount) {
            break;
        }

        formattedDecimal[charPos++] = (char) ('0' + ncp->uArray[ncp->getCount++]);
    }

    if (k == expt - 1){
        formattedDecimal[charPos++] = '0';
    }
    formattedDecimal[charPos++] = 'E';
    {
      char expt_buf[BUFSIZ];
      snprintf(expt_buf, sizeof(expt_buf), "%d", expt);
      strncpy(buf, formattedDecimal, charPos);
      buf[charPos] = 0;
      strcat(buf, expt_buf);
      return;
    }
}

static void freeFormat(NumberConverter *ncp, char *buf, int buf_len) {
    // corresponds to process "Free-Format"
    char formattedDecimal[25];
    // the position the next character is to be inserted into
    // formattedDecimal
    int charPos = 0;
    int k = ncp->firstK;
    if (k < 0) {
        int i;

        formattedDecimal[0] = '0';
        formattedDecimal[1] = '.';
        charPos += 2;
        for (i = k + 1; i < 0; i++) {
            formattedDecimal[charPos++] = '0';
        }
    }

    int U = ncp->uArray[ncp->getCount++];
    do {
        if (U != -1) {
            formattedDecimal[charPos++] = (char) ('0' + U);
        } else if (k >= -1) {
            formattedDecimal[charPos++] = '0';
        }

        if (k == 0) {
            formattedDecimal[charPos++] = '.';
        }

        k--;
        U = ncp->getCount < ncp->setCount ? ncp->uArray[ncp->getCount++] : -1;
    } while (U != -1 || k >= -1);
    strncpy(buf, formattedDecimal, charPos);
    buf[charPos] = 0;
    return;
}

static void longDigitGenerator(NumberConverter *ncp, int64_t f, int e, boolean isDenormalized,
        boolean mantissaIsZero, int p) {
    int64_t R, S, M;
    if (e >= 0) {
        M = 1LL << e;
        if (!mantissaIsZero) {
            R = f << (e + 1);
            S = 2;
        } else {
            R = f << (e + 2);
            S = 4;
        }
    } else {
        M = 1;
        if (isDenormalized || !mantissaIsZero) {
            R = f << 1;
            S = 1LL << (1 - e);
        } else {
            R = f << 2;
            S = 1LL << (2 - e);
        }
    }

    int k = (int) /*Math.*/ceil((e + p - 1) * invLogOfTenBaseTwo - 1e-10);

    if (k > 0) {
        S = S * TEN_TO_THE[k];
    } else if (k < 0) {
        int64_t scale = TEN_TO_THE[-k];
        R = R * scale;
        M = M == 1 ? scale : M * scale;
    }

    if (R + M > S) { // was M_plus
        ncp->firstK = k;
    } else {
        ncp->firstK = k - 1;
        R = R * 10;
        M = M * 10;
    }

    ncp->getCount = ncp->setCount = 0; // reset indices
    boolean low, high;
    int U;
    int64_t Si[4] = { S, S << 1, S << 2, S << 3 };
    while (true) {
        int i;
        int64_t remainder;

        // set U to be floor (R / S) and R to be the remainder
        // using a kind of "binary search" to find the answer.
        // It's a lot quicker than actually dividing since we know
        // the answer will be between 0 and 10
        U = 0;
        for (i = 3; i >= 0; i--) {
            remainder = R - Si[i];
            // fprintf(stdout, "S=%lld i=%d R=%lld Si=%lld remainder=%lld U=%d\n", S, i, R, Si[i], remainder, U);

            if (remainder >= 0) {
                R = remainder;
                U += (1 << i);
            }
        }

        low = R < M; // was M_minus
        high = R + M > S; // was M_plus

        if (low || high) {
            break;
        }

        R = R * 10;
        M = M * 10;
        ncp->uArray[ncp->setCount++] = U;
    }
    if (low && !high) {
        ncp->uArray[ncp->setCount++] = U;
    } else if (high && !low) {
        ncp->uArray[ncp->setCount++] = U + 1;
    } else if ((R << 1) < S) {
        ncp->uArray[ncp->setCount++] = U;
    } else {
        ncp->uArray[ncp->setCount++] = U + 1;
    }
}

void convert_double(double input, char *buf, int buf_len) {
  NumberConverter nc;
  initialize_NumberConverter(&nc);
  convertD(&nc, input, buf, buf_len);
}

void convert_float(float input, char *buf, int buf_len) {
  NumberConverter nc;
  initialize_NumberConverter(&nc);
  convertF(&nc, input, buf, buf_len);
}

/* cbigint.c { */
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#include <string.h>

// typedef int64_t I_64;
// typedef uint64_t U_64;
typedef int32_t I_32;
typedef uint32_t U_32;

/* cbigint.h { */
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#if !defined(cbigint_h)
#define cbigint_h

/* fltconst.h { */

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#if !defined(fltconst_h)
#define fltconst_h

/* hycomp.h { */
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#if !defined(hycomp_h)
#define hycomp_h

#if !defined(LINUX)
#define LINUX 1
#endif

/**
 * USE_PROTOTYPES:         Use full ANSI prototypes.
 *
 * CLOCK_PRIMS:            We want the timer/clock prims to be used
 *
 * LITTLE_ENDIAN:          This is for the intel machines or other
 *                         little endian processors. Defaults to big endian.
 *
 * NO_LVALUE_CASTING:      This is for compilers that don't like the left side
 *                         of assigns to be cast.  It hacks around to do the
 *                         right thing.
 *
 * ATOMIC_FLOAT_ACCESS:    So that float operations will work.
 *
 * LINKED_USER_PRIMITIVES: Indicates that user primitives are statically linked
 *                         with the VM executeable.
 *
 * OLD_SPACE_SIZE_DIFF:    The 68k uses a different amount of old space.
 *                         This "legitimizes" the change.
 *
 * SIMPLE_SIGNAL:          For machines that don't use real signals in C.
 *                         (eg: PC, 68k)
 *
 * OS_NAME_LOOKUP:         Use nlist to lookup user primitive addresses.
 *
 * VMCALL:                 Tag for all functions called by the VM.
 *
 * VMAPICALL:              Tag for all functions called via the PlatformFunction
 *                         callWith: mechanism.
 *
 * SYS_FLOAT:              For some math functions where extended types (80 or 96 bits) are returned
 *                         Most platforms return as a double
 *
 * FLOAT_EXTENDED:         If defined, the type name for extended precision floats.
 *
 * PLATFORM_IS_ASCII:      Must be defined if the platform is ASCII
 *
 * EXE_EXTENSION_CHAR:     the executable has a delimiter that we want to stop at as part of argv[0].
 */

 /**
 * By default order doubles in the native (that is big/little endian) ordering.
 */

#define HY_PLATFORM_DOUBLE_ORDER

/**
 * Define common types:
 * <ul>
 * <li><code>U_32 / I_32</code>  - unsigned/signed 32 bits</li>
 * <li><code>U_16 / I_16</code>  - unsigned/signed 16 bits</li>
 * <li><code>U_8  / I_8</code>   - unsigned/signed 8 bits (bytes -- not to be
 *                                 confused with char)</li>
 * </ul>
 */

typedef          int   I_32;
typedef          short I_16;
typedef signed   char  I_8; /* chars can be unsigned */
typedef unsigned int   U_32;
typedef unsigned short U_16;
typedef unsigned char  U_8;

/**
 * Define platform specific types:
 * <ul>
 * <li><code>U_64 / I_64</code>  - unsigned/signed 64 bits</li>
 * </ul>
 */

#if defined(LINUX) || defined(FREEBSD) || defined(AIX)

#define DATA_TYPES_DEFINED

/* NOTE: Linux supports different processors -- do not assume 386 */
    #if defined(HYX86_64) || defined(HYIA64) || defined(HYPPC64) || defined(HYS390X)

        typedef unsigned long int U_64;         /* 64bits */
        typedef          long int I_64;
        #define TOC_UNWRAP_ADDRESS(wrappedPointer) ((void *) (wrappedPointer)[0])
        #define TOC_STORE_TOC(dest,wrappedPointer) (dest = ((UDATA*)wrappedPointer)[1])

        #define HY_WORD64

    #else

        typedef unsigned long long U_64;
        typedef          long long I_64;

    #endif

    #if defined(HYS390X) || defined(HYS390) || defined(HYPPC64) || defined(HYPPC32)
        #define HY_BIG_ENDIAN
    #else
        #define HY_LITTLE_ENDIAN
    #endif

    #if defined(HYPPC32)
        #define VA_PTR(valist) (&valist[0])
    #endif

    typedef double SYS_FLOAT;
    #define HYCONST64(x) x##LL
    #define NO_LVALUE_CASTING
    #define FLOAT_EXTENDED  long double
    #define PLATFORM_IS_ASCII
    #define PLATFORM_LINE_DELIMITER "\012"
    #define DIR_SEPARATOR '/'
    #define DIR_SEPARATOR_STR "/"

/**
 * No priorities on Linux
 */

    #define HY_PRIORITY_MAP {0,0,0,0,0,0,0,0,0,0,0,0}

    typedef U_32 BOOLEAN;

#endif

/* Win32 - Windows 3.1 & NT using Win32 */
#if defined(WIN32)

    #define HY_LITTLE_ENDIAN

/* Define 64-bit integers for Windows */
    typedef __int64 I_64;
    typedef unsigned __int64 U_64;

    typedef double SYS_FLOAT;
    #define NO_LVALUE_CASTING
    #define VMAPICALL _stdcall
    #define VMCALL _cdecl
    #define EXE_EXTENSION_CHAR  '.'

    #define DIR_SEPARATOR '\\'
    #define DIR_SEPARATOR_STR "\\"

/* Modifications for the Alpha running WIN-NT */
    #if defined(_ALPHA_)
        #undef small                    /* defined as char in rpcndr.h */
        typedef double FLOAT_EXTENDED;
    #endif

    #define HY_PRIORITY_MAP { \
      THREAD_PRIORITY_IDLE,             /* 0 */\
      THREAD_PRIORITY_LOWEST,           /* 1 */\
      THREAD_PRIORITY_BELOW_NORMAL,     /* 2 */\
      THREAD_PRIORITY_BELOW_NORMAL,     /* 3 */\
      THREAD_PRIORITY_BELOW_NORMAL,     /* 4 */\
      THREAD_PRIORITY_NORMAL,           /* 5 */\
      THREAD_PRIORITY_ABOVE_NORMAL,     /* 6 */\
      THREAD_PRIORITY_ABOVE_NORMAL,     /* 7 */\
      THREAD_PRIORITY_ABOVE_NORMAL,     /* 8 */\
      THREAD_PRIORITY_ABOVE_NORMAL,     /* 9 */\
      THREAD_PRIORITY_HIGHEST,          /*10 */\
      THREAD_PRIORITY_TIME_CRITICAL     /*11 */}

#endif /* defined(WIN32) */

#if !defined(VMCALL)
    #define VMCALL
    #define VMAPICALL
#endif
#define PVMCALL VMCALL *

#define GLOBAL_DATA(symbol) ((void*)&(symbol))
#define GLOBAL_TABLE(symbol) GLOBAL_DATA(symbol)

/**
 * Define platform specific types:
 * <ul>
 * <li><code>UDATA</code>        - unsigned data, can be used as an integer or
 *                                 pointer storage</li>
 * <li><code>IDATA</code>        - signed data, can be used as an integer or
 *                                 pointer storage</li>
 * </ul>
 */
/* FIXME: POINTER64 */
#if defined(HYX86_64) || defined(HYIA64) || defined(HYPPC64) || defined(HYS390X) || defined(POINTER64)

typedef I_64 IDATA;
typedef U_64 UDATA;

#else /* this is default for non-64bit systems */

typedef I_32 IDATA;
typedef U_32 UDATA;

#endif /* defined(HYX86_64) */

#if !defined(DATA_TYPES_DEFINED)
/* no generic U_64 or I_64 */

/* don't typedef BOOLEAN since it's already def'ed on Win32 */
#define BOOLEAN UDATA

#ifndef HY_BIG_ENDIAN
#define HY_LITTLE_ENDIAN
#endif

#endif

#if !defined(HYCONST64)
#define HYCONST64(x) x##L
#endif

#if !defined(HY_DEFAULT_SCHED)

/**
 * By default, pthreads platforms use the <code>SCHED_OTHER</code> thread
 * scheduling policy.
 */

#define HY_DEFAULT_SCHED SCHED_OTHER
#endif

#if !defined(HY_PRIORITY_MAP)

/**
 * If no priority map if provided, priorities will be determined
 * algorithmically.
 */

#endif

#if !defined(FALSE)
#define FALSE   ((BOOLEAN) 0)
#if !defined(TRUE)
#define TRUE    ((BOOLEAN) (!FALSE))
#endif
#endif

#if !defined(NULL)
#if defined(__cplusplus)
#define NULL    (0)
#else
#define NULL    ((void *)0)
#endif
#endif
#define USE_PROTOTYPES
#if defined(USE_PROTOTYPES)
#define PROTOTYPE(x)  x
#define VARARGS   , ...
#else
#define PROTOTYPE(x)  ()
#define VARARGS
#endif

/**
 * Assign the default line delimiter, if it was not set.
 */

#if !defined(PLATFORM_LINE_DELIMITER)
#define PLATFORM_LINE_DELIMITER "\015\012"
#endif

/**
 * Set the max path length, if it was not set.
 */

#if !defined(MAX_IMAGE_PATH_LENGTH)
#define MAX_IMAGE_PATH_LENGTH (2048)
#endif
typedef double ESDOUBLE;
typedef float ESSINGLE;

/**
 * Helpers for U_64s.
 */

#define CLEAR_U64(u64)  (u64 = (U_64)0)
#define LOW_LONG(l) (*((U_32 *) &(l)))
#define HIGH_LONG(l)  (*(((U_32 *) &(l)) + 1))
#define I8(x)       ((I_8) (x))
#define I8P(x)      ((I_8 *) (x))
#define U16(x)      ((U_16) (x))
#define I16(x)      ((I_16) (x))
#define I16P(x)     ((I_16 *) (x))
#define U32(x)      ((U_32) (x))
#define I32(x)      ((I_32) (x))
#define I32P(x)     ((I_32 *) (x))
#define U16P(x)     ((U_16 *) (x))
#define U32P(x)     ((U_32 *) (x))
#define OBJP(x)     ((HyObject *) (x))
#define OBJPP(x)    ((HyObject **) (x))
#define OBJPPP(x)   ((HyObject ***) (x))
#define CLASSP(x)   ((Class *) (x))
#define CLASSPP(x)  ((Class **) (x))
#define BYTEP(x)    ((BYTE *) (x))

/**
 * Test - was conflicting with OS2.h
 */

#define ESCHAR(x)   ((CHARACTER) (x))
#define FLT(x)      ((FLOAT) x)
#define FLTP(x)     ((FLOAT *) (x))
#if defined(NO_LVALUE_CASTING)
#define LI8(x)      (*((I_8 *) &(x)))
#define LI8P(x)     (*((I_8 **) &(x)))
#define LU16(x)     (*((U_16 *) &(x)))
#define LI16(x)     (*((I_16 *) &(x)))
#define LU32(x)     (*((U_32 *) &(x)))
#define LI32(x)     (*((I_32 *) &(x)))
#define LI32P(x)    (*((I_32 **) &(x)))
#define LU16P(x)    (*((U_16 **) &(x)))
#define LU32P(x)    (*((U_32 **) &(x)))
#define LOBJP(x)    (*((HyObject **) &(x)))
#define LOBJPP(x)   (*((HyObject ***) &(x)))
#define LOBJPPP(x)  (*((HyObject ****) &(x))
#define LCLASSP(x)  (*((Class **) &(x)))
#define LBYTEP(x)   (*((BYTE **) &(x)))
#define LCHAR(x)    (*((CHARACTER) &(x)))
#define LFLT(x)     (*((FLOAT) &x))
#define LFLTP(x)    (*((FLOAT *) &(x)))
#else
#define LI8(x)      I8((x))
#define LI8P(x)     I8P((x))
#define LU16(x)     U16((x))
#define LI16(x)     I16((x))
#define LU32(x)     U32((x))
#define LI32(x)     I32((x))
#define LI32P(x)    I32P((x))
#define LU16P(x)    U16P((x))
#define LU32P(x)    U32P((x))
#define LOBJP(x)    OBJP((x))
#define LOBJPP(x)   OBJPP((x))
#define LOBJPPP(x)  OBJPPP((x))
#define LIOBJP(x)   IOBJP((x))
#define LCLASSP(x)  CLASSP((x))
#define LBYTEP(x)   BYTEP((x))
#define LCHAR(x)    CHAR((x))
#define LFLT(x)     FLT((x))
#define LFLTP(x)    FLTP((x))
#endif

/**
 * Macros for converting between words and longs and accessing bits.
 */

#define HIGH_WORD(x)  U16(U32((x)) >> 16)
#define LOW_WORD(x)   U16(U32((x)) & 0xFFFF)
#define LOW_BIT(o)    (U32((o)) & 1)
#define LOW_2_BITS(o) (U32((o)) & 3)
#define LOW_3_BITS(o) (U32((o)) & 7)
#define LOW_4_BITS(o) (U32((o)) & 15)
#define MAKE_32(h, l) ((U32((h)) << 16) | U32((l)))
#define MAKE_64(h, l) ((((I_64)(h)) << 32) | (l))
#if defined(__cplusplus)
#define HY_CFUNC "C"
#define HY_CDATA "C"
#else
#define HY_CFUNC
#define HY_CDATA
#endif

/**
 * Macros for tagging functions which read/write the vm thread.
 */

#define READSVMTHREAD
#define WRITESVMTHREAD
#define REQUIRESSTACKFRAME

/**
 * Macro for tagging functions, which never return.
 */

#if defined(__GNUC__)

/**
 * On GCC, we can actually pass this information on to the compiler.
 */

#define NORETURN __attribute__((noreturn))
#else
#define NORETURN
#endif

/**
 * On some systems va_list is an array type.  This is probably in
 * violation of the ANSI C spec, but it's not entirely clear.  Because of
 * this, we end up with an undesired extra level of indirection if we take
 * the address of a va_list argument. 
 *
 * To get it right, always use the VA_PTR macro
 */

#if !defined(VA_PTR)
#define VA_PTR(valist) (&valist)
#endif
#if !defined(TOC_UNWRAP_ADDRESS)
#define TOC_UNWRAP_ADDRESS(wrappedPointer) (wrappedPointer)
#endif

#if !defined(TOC_STORE_TOC)
#define TOC_STORE_TOC(dest,wrappedPointer)
#endif
/**
 * Macros for accessing I_64 values.
 */

#if defined(ATOMIC_LONG_ACCESS)
#define PTR_LONG_STORE(dstPtr, aLongPtr) ((*U32P(dstPtr) = *U32P(aLongPtr)), (*(U32P(dstPtr)+1) = *(U32P(aLongPtr)+1)))
#define PTR_LONG_VALUE(dstPtr, aLongPtr) ((*U32P(aLongPtr) = *U32P(dstPtr)), (*(U32P(aLongPtr)+1) = *(U32P(dstPtr)+1)))
#else
#define PTR_LONG_STORE(dstPtr, aLongPtr) (*(dstPtr) = *(aLongPtr))
#define PTR_LONG_VALUE(dstPtr, aLongPtr) (*(aLongPtr) = *(dstPtr))
#endif

/**
 * Macro used when declaring tables which require relocations.
 */

#if !defined(HYCONST_TABLE)
#define HYCONST_TABLE const
#endif

/**
 * ANSI qsort is not always available.
 */

#if !defined(HY_SORT)
#define HY_SORT(base, nmemb, size, compare) qsort((base), (nmemb), (size), (compare))
#endif

#endif /* hycomp_h */

/* hycomp.h } */

/* IEEE floats consist of: sign bit, exponent field, significand field
    single:  31 = sign bit, 30..23 = exponent (8 bits), 22..0 = significand (23 bits)
    double:  63 = sign bit, 62..52 = exponent (11 bits), 51..0 = significand (52 bits)
    inf                ==    (all exponent bits set) and (all mantissa bits clear)
    nan                ==    (all exponent bits set) and (at least one mantissa bit set)
    finite             ==    (at least one exponent bit clear)
    zero               ==    (all exponent bits clear) and (all mantissa bits clear)
    denormal           ==    (all exponent bits clear) and (at least one mantissa bit set)
    positive           ==    sign bit clear
    negative           ==    sign bit set
*/
#define MAX_U32_DOUBLE (ESDOUBLE) (4294967296.0)    /* 2^32 */
#define MAX_U32_SINGLE (ESSINGLE) (4294967296.0)    /* 2^32 */
#define HY_POS_PI      (ESDOUBLE) (3.141592653589793)

#ifdef HY_LITTLE_ENDIAN
#ifdef HY_PLATFORM_DOUBLE_ORDER
#define DOUBLE_LO_OFFSET        0
#define DOUBLE_HI_OFFSET        1
#else
#define DOUBLE_LO_OFFSET        1
#define DOUBLE_HI_OFFSET        0
#endif
#define LONG_LO_OFFSET          0
#define LONG_HI_OFFSET          1
#else
#ifdef HY_PLATFORM_DOUBLE_ORDER
#define DOUBLE_LO_OFFSET        1
#define DOUBLE_HI_OFFSET        0
#else
#define DOUBLE_LO_OFFSET        0
#define DOUBLE_HI_OFFSET        1
#endif
#define LONG_LO_OFFSET          1
#define LONG_HI_OFFSET          0
#endif

#define RETURN_FINITE           0
#define RETURN_NAN              1
#define RETURN_POS_INF          2
#define RETURN_NEG_INF          3
#define DOUBLE_SIGN_MASK_HI     0x80000000
#define DOUBLE_EXPONENT_MASK_HI 0x7FF00000
#define DOUBLE_MANTISSA_MASK_LO 0xFFFFFFFF
#define DOUBLE_MANTISSA_MASK_HI 0x000FFFFF
#define SINGLE_SIGN_MASK        0x80000000
#define SINGLE_EXPONENT_MASK    0x7F800000
#define SINGLE_MANTISSA_MASK    0x007FFFFF
#define SINGLE_NAN_BITS         (SINGLE_EXPONENT_MASK | 0x00400000)

typedef union u64u32dbl_tag {
    U_64    u64val;
    U_32    u32val[2];
    I_32    i32val[2];
    double  dval;
} U64U32DBL;

/* Replace P_FLOAT_HI and P_FLOAT_LOW */
/* These macros are used to access the high and low 32-bit parts of a double (64-bit) value. */
#define LOW_U32_FROM_DBL_PTR(dblptr) (((U64U32DBL *)(dblptr))->u32val[DOUBLE_LO_OFFSET])
#define HIGH_U32_FROM_DBL_PTR(dblptr) (((U64U32DBL *)(dblptr))->u32val[DOUBLE_HI_OFFSET])
#define LOW_I32_FROM_DBL_PTR(dblptr) (((U64U32DBL *)(dblptr))->i32val[DOUBLE_LO_OFFSET])
#define HIGH_I32_FROM_DBL_PTR(dblptr) (((U64U32DBL *)(dblptr))->i32val[DOUBLE_HI_OFFSET])
#define LOW_U32_FROM_DBL(dbl) LOW_U32_FROM_DBL_PTR(&(dbl))
#define HIGH_U32_FROM_DBL(dbl) HIGH_U32_FROM_DBL_PTR(&(dbl))
#define LOW_I32_FROM_DBL(dbl) LOW_I32_FROM_DBL_PTR(&(dbl))
#define HIGH_I32_FROM_DBL(dbl) HIGH_I32_FROM_DBL_PTR(&(dbl))
#define LOW_U32_FROM_LONG64_PTR(long64ptr) (((U64U32DBL *)(long64ptr))->u32val[LONG_LO_OFFSET])
#define HIGH_U32_FROM_LONG64_PTR(long64ptr) (((U64U32DBL *)(long64ptr))->u32val[LONG_HI_OFFSET])
#define LOW_I32_FROM_LONG64_PTR(long64ptr) (((U64U32DBL *)(long64ptr))->i32val[LONG_LO_OFFSET])
#define HIGH_I32_FROM_LONG64_PTR(long64ptr) (((U64U32DBL *)(long64ptr))->i32val[LONG_HI_OFFSET])
#define LOW_U32_FROM_LONG64(long64) LOW_U32_FROM_LONG64_PTR(&(long64))
#define HIGH_U32_FROM_LONG64(long64) HIGH_U32_FROM_LONG64_PTR(&(long64))
#define LOW_I32_FROM_LONG64(long64) LOW_I32_FROM_LONG64_PTR(&(long64))
#define HIGH_I32_FROM_LONG64(long64) HIGH_I32_FROM_LONG64_PTR(&(long64))
#define IS_ZERO_DBL_PTR(dblptr) ((LOW_U32_FROM_DBL_PTR(dblptr) == 0) && ((HIGH_U32_FROM_DBL_PTR(dblptr) == 0) || (HIGH_U32_FROM_DBL_PTR(dblptr) == DOUBLE_SIGN_MASK_HI)))
#define IS_ONE_DBL_PTR(dblptr) ((HIGH_U32_FROM_DBL_PTR(dblptr) == 0x3ff00000 || HIGH_U32_FROM_DBL_PTR(dblptr) == 0xbff00000) && (LOW_U32_FROM_DBL_PTR(dblptr) == 0))
#define IS_NAN_DBL_PTR(dblptr) (((HIGH_U32_FROM_DBL_PTR(dblptr) & DOUBLE_EXPONENT_MASK_HI) == DOUBLE_EXPONENT_MASK_HI) && (LOW_U32_FROM_DBL_PTR(dblptr) | (HIGH_U32_FROM_DBL_PTR(dblptr) & DOUBLE_MANTISSA_MASK_HI)))
#define IS_INF_DBL_PTR(dblptr) (((HIGH_U32_FROM_DBL_PTR(dblptr) & (DOUBLE_EXPONENT_MASK_HI|DOUBLE_MANTISSA_MASK_HI)) == DOUBLE_EXPONENT_MASK_HI) && (LOW_U32_FROM_DBL_PTR(dblptr) == 0))
#define IS_DENORMAL_DBL_PTR(dblptr) (((HIGH_U32_FROM_DBL_PTR(dblptr) & DOUBLE_EXPONENT_MASK_HI) == 0) && ((HIGH_U32_FROM_DBL_PTR(dblptr) & DOUBLE_MANTISSA_MASK_HI) != 0 || (LOW_U32_FROM_DBL_PTR(dblptr) != 0)))
#define IS_FINITE_DBL_PTR(dblptr) ((HIGH_U32_FROM_DBL_PTR(dblptr) & DOUBLE_EXPONENT_MASK_HI) < DOUBLE_EXPONENT_MASK_HI)
#define IS_POSITIVE_DBL_PTR(dblptr) ((HIGH_U32_FROM_DBL_PTR(dblptr) & DOUBLE_SIGN_MASK_HI) == 0)
#define IS_NEGATIVE_DBL_PTR(dblptr) ((HIGH_U32_FROM_DBL_PTR(dblptr) & DOUBLE_SIGN_MASK_HI) != 0)
#define IS_NEGATIVE_MAX_DBL_PTR(dblptr) ((HIGH_U32_FROM_DBL_PTR(dblptr) == 0xFFEFFFFF) && (LOW_U32_FROM_DBL_PTR(dblptr) == 0xFFFFFFFF))
#define IS_ZERO_DBL(dbl) IS_ZERO_DBL_PTR(&(dbl))
#define IS_ONE_DBL(dbl) IS_ONE_DBL_PTR(&(dbl))
#define IS_NAN_DBL(dbl) IS_NAN_DBL_PTR(&(dbl))
#define IS_INF_DBL(dbl) IS_INF_DBL_PTR(&(dbl))
#define IS_DENORMAL_DBL(dbl) IS_DENORMAL_DBL_PTR(&(dbl))
#define IS_FINITE_DBL(dbl) IS_FINITE_DBL_PTR(&(dbl))
#define IS_POSITIVE_DBL(dbl) IS_POSITIVE_DBL_PTR(&(dbl))
#define IS_NEGATIVE_DBL(dbl) IS_NEGATIVE_DBL_PTR(&(dbl))
#define IS_NEGATIVE_MAX_DBL(dbl) IS_NEGATIVE_MAX_DBL_PTR(&(dbl))
#define IS_ZERO_SNGL_PTR(fltptr)  ((*U32P((fltptr)) & (U_32)~SINGLE_SIGN_MASK) == (U_32)0)
#define IS_ONE_SNGL_PTR(fltptr) ((*U32P((fltptr)) == 0x3f800000) || (*U32P((fltptr)) == 0xbf800000))
#define IS_NAN_SNGL_PTR(fltptr)  ((*U32P((fltptr)) & (U_32)~SINGLE_SIGN_MASK) > (U_32)SINGLE_EXPONENT_MASK)
#define IS_INF_SNGL_PTR(fltptr)  ((*U32P((fltptr)) & (U_32)~SINGLE_SIGN_MASK) == (U_32)SINGLE_EXPONENT_MASK)
#define IS_DENORMAL_SNGL_PTR(fltptr)  (((*U32P((fltptr)) & (U_32)~SINGLE_SIGN_MASK)-(U_32)1) < (U_32)SINGLE_MANTISSA_MASK)
#define IS_FINITE_SNGL_PTR(fltptr)  ((*U32P((fltptr)) & (U_32)~SINGLE_SIGN_MASK) < (U_32)SINGLE_EXPONENT_MASK)
#define IS_POSITIVE_SNGL_PTR(fltptr)  ((*U32P((fltptr)) & (U_32)SINGLE_SIGN_MASK) == (U_32)0)
#define IS_NEGATIVE_SNGL_PTR(fltptr)  ((*U32P((fltptr)) & (U_32)SINGLE_SIGN_MASK) != (U_32)0)
#define IS_ZERO_SNGL(flt) IS_ZERO_SNGL_PTR(&(flt))
#define IS_ONE_SNGL(flt) IS_ONE_SNGL_PTR(&(flt))
#define IS_NAN_SNGL(flt) IS_NAN_SNGL_PTR(&(flt))
#define IS_INF_SNGL(flt) IS_INF_SNGL_PTR(&(flt))
#define IS_DENORMAL_SNGL(flt) IS_DENORMAL_SNGL_PTR(&(flt))
#define IS_FINITE_SNGL(flt) IS_FINITE_SNGL_PTR(&(flt))
#define IS_POSITIVE_SNGL(flt) IS_POSITIVE_SNGL_PTR(&(flt))
#define IS_NEGATIVE_SNGL(flt) IS_NEGATIVE_SNGL_PTR(&(flt))
#define SET_NAN_DBL_PTR(dblptr) HIGH_U32_FROM_DBL_PTR(dblptr) = (DOUBLE_EXPONENT_MASK_HI | 0x00080000); LOW_U32_FROM_DBL_PTR(dblptr) = 0
#define SET_PZERO_DBL_PTR(dblptr) HIGH_U32_FROM_DBL_PTR(dblptr) = 0; LOW_U32_FROM_DBL_PTR(dblptr) = 0
#define SET_NZERO_DBL_PTR(dblptr) HIGH_U32_FROM_DBL_PTR(dblptr) = DOUBLE_SIGN_MASK_HI; LOW_U32_FROM_DBL_PTR(dblptr) = 0
#define SET_PINF_DBL_PTR(dblptr) HIGH_U32_FROM_DBL_PTR(dblptr) = DOUBLE_EXPONENT_MASK_HI; LOW_U32_FROM_DBL_PTR(dblptr) = 0
#define SET_NINF_DBL_PTR(dblptr) HIGH_U32_FROM_DBL_PTR(dblptr) = (DOUBLE_EXPONENT_MASK_HI | DOUBLE_SIGN_MASK_HI); LOW_U32_FROM_DBL_PTR(dblptr) = 0
#define SET_NAN_SNGL_PTR(fltptr)   *U32P((fltptr)) = ((U_32)SINGLE_NAN_BITS)
#define SET_PZERO_SNGL_PTR(fltptr) *U32P((fltptr)) = 0
#define SET_NZERO_SNGL_PTR(fltptr) *U32P((fltptr)) = SINGLE_SIGN_MASK
#define SET_PINF_SNGL_PTR(fltptr)  *U32P((fltptr)) = SINGLE_EXPONENT_MASK
#define SET_NINF_SNGL_PTR(fltptr)  *U32P((fltptr)) = (SINGLE_EXPONENT_MASK | SINGLE_SIGN_MASK)

/* on some platforms (HP720) we cannot reference an unaligned float.  Build them by hand, one U_32 at a time. */
#if defined(ATOMIC_FLOAT_ACCESS)
#define PTR_DOUBLE_STORE(dstPtr, aDoublePtr) HIGH_U32_FROM_DBL_PTR(dstPtr) = HIGH_U32_FROM_DBL_PTR(aDoublePtr); LOW_U32_FROM_DBL_PTR(dstPtr) = LOW_U32_FROM_DBL_PTR(aDoublePtr)
#define PTR_DOUBLE_VALUE(dstPtr, aDoublePtr) HIGH_U32_FROM_DBL_PTR(aDoublePtr) = HIGH_U32_FROM_DBL_PTR(dstPtr); LOW_U32_FROM_DBL_PTR(aDoublePtr) = LOW_U32_FROM_DBL_PTR(dstPtr)
#else
#define PTR_DOUBLE_STORE(dstPtr, aDoublePtr) (*(dstPtr) = *(aDoublePtr))
#define PTR_DOUBLE_VALUE(dstPtr, aDoublePtr) (*(aDoublePtr) = *(dstPtr))
#endif

#define STORE_LONG(dstPtr, hi, lo) HIGH_U32_FROM_LONG64_PTR(dstPtr) = (hi); LOW_U32_FROM_LONG64_PTR(dstPtr) = (lo)
#define PTR_SINGLE_VALUE(dstPtr, aSinglePtr) (*U32P(aSinglePtr) = *U32P(dstPtr))
#define PTR_SINGLE_STORE(dstPtr, aSinglePtr) *((U_32 *)(dstPtr)) = (*U32P(aSinglePtr))

#endif     /* fltconst_h */

/* fltconst.h } */

#define LOW_U32_FROM_VAR(u64)     LOW_U32_FROM_LONG64(u64)
#define LOW_U32_FROM_PTR(u64ptr)  LOW_U32_FROM_LONG64_PTR(u64ptr)
#define HIGH_U32_FROM_VAR(u64)    HIGH_U32_FROM_LONG64(u64)
#define HIGH_U32_FROM_PTR(u64ptr) HIGH_U32_FROM_LONG64_PTR(u64ptr)
#if defined(__cplusplus)
extern "C"
{
#endif
  void multiplyHighPrecision (U_64 * arg1, IDATA length1, U_64 * arg2,
                              IDATA length2, U_64 * result, IDATA length);
  U_32 simpleAppendDecimalDigitHighPrecision (U_64 * arg1, IDATA length,
                                              U_64 digit);
  jdouble toDoubleHighPrecision (U_64 * arg, IDATA length);
  IDATA tenToTheEHighPrecision (U_64 * result, IDATA length, jint e);
  U_64 doubleMantissa (jdouble z);
  IDATA compareHighPrecision (U_64 * arg1, IDATA length1, U_64 * arg2,
                              IDATA length2);
  IDATA highestSetBitHighPrecision (U_64 * arg, IDATA length);
  void subtractHighPrecision (U_64 * arg1, IDATA length1, U_64 * arg2,
                              IDATA length2);
  IDATA doubleExponent (jdouble z);
  U_32 simpleMultiplyHighPrecision (U_64 * arg1, IDATA length, U_64 arg2);
  IDATA addHighPrecision (U_64 * arg1, IDATA length1, U_64 * arg2,
                          IDATA length2);
  void simpleMultiplyAddHighPrecisionBigEndianFix (U_64 * arg1, IDATA length,
                                                   U_64 arg2, U_32 * result);
  IDATA lowestSetBit (U_64 * y);
  IDATA timesTenToTheEHighPrecision (U_64 * result, IDATA length, jint e);
  void simpleMultiplyAddHighPrecision (U_64 * arg1, IDATA length, U_64 arg2,
                                       U_32 * result);
  IDATA highestSetBit (U_64 * y);
  IDATA lowestSetBitHighPrecision (U_64 * arg, IDATA length);
  void simpleShiftLeftHighPrecision (U_64 * arg1, IDATA length, IDATA arg2);
  UDATA floatMantissa (jfloat z);
  U_64 simpleMultiplyHighPrecision64 (U_64 * arg1, IDATA length, U_64 arg2);
  IDATA simpleAddHighPrecision (U_64 * arg1, IDATA length, U_64 arg2);
  IDATA floatExponent (jfloat z);
#if defined(__cplusplus)
}
#endif
#endif                          /* cbigint_h */

/* cbigint.h } */

#if defined(LINUX) || defined(FREEBSD) || defined(ZOS) || defined(MACOSX) || defined(AIX)
#define USE_LL
#endif

#ifdef HY_LITTLE_ENDIAN
#define at(i) (i)
#else
#define at(i) ((i)^1)
/* the sequence for halfAt is -1, 2, 1, 4, 3, 6, 5, 8... */
/* and it should correspond to 0, 1, 2, 3, 4, 5, 6, 7... */
#define halfAt(i) (-((-(i)) ^ 1))
#endif

#define HIGH_IN_U64(u64) ((u64) >> 32)
#if defined(USE_LL)
#define LOW_IN_U64(u64) ((u64) & 0x00000000FFFFFFFFLL)
#else
#if defined(USE_L)
#define LOW_IN_U64(u64) ((u64) & 0x00000000FFFFFFFFL)
#else
#define LOW_IN_U64(u64) ((u64) & 0x00000000FFFFFFFF)
#endif /* USE_L */
#endif /* USE_LL */

#if defined(USE_LL)
#define TEN_E1 (0xALL)
#define TEN_E2 (0x64LL)
#define TEN_E3 (0x3E8LL)
#define TEN_E4 (0x2710LL)
#define TEN_E5 (0x186A0LL)
#define TEN_E6 (0xF4240LL)
#define TEN_E7 (0x989680LL)
#define TEN_E8 (0x5F5E100LL)
#define TEN_E9 (0x3B9ACA00LL)
#define TEN_E19 (0x8AC7230489E80000LL)
#else
#if defined(USE_L)
#define TEN_E1 (0xAL)
#define TEN_E2 (0x64L)
#define TEN_E3 (0x3E8L)
#define TEN_E4 (0x2710L)
#define TEN_E5 (0x186A0L)
#define TEN_E6 (0xF4240L)
#define TEN_E7 (0x989680L)
#define TEN_E8 (0x5F5E100L)
#define TEN_E9 (0x3B9ACA00L)
#define TEN_E19 (0x8AC7230489E80000L)
#else
#define TEN_E1 (0xA)
#define TEN_E2 (0x64)
#define TEN_E3 (0x3E8)
#define TEN_E4 (0x2710)
#define TEN_E5 (0x186A0)
#define TEN_E6 (0xF4240)
#define TEN_E7 (0x989680)
#define TEN_E8 (0x5F5E100)
#define TEN_E9 (0x3B9ACA00)
#define TEN_E19 (0x8AC7230489E80000)
#endif /* USE_L */
#endif /* USE_LL */

#define TIMES_TEN(x) (((x) << 3) + ((x) << 1))
#define bitSection(x, mask, shift) (((x) & (mask)) >> (shift))
#define DOUBLE_TO_LONGBITS(dbl) (*((U_64 *)(&dbl)))
#define FLOAT_TO_INTBITS(flt) (*((U_32 *)(&flt)))
#define CREATE_DOUBLE_BITS(normalizedM, e) (((normalizedM) & MANTISSA_MASK) | (((U_64)((e) + E_OFFSET)) << 52))

#if defined(USE_LL)
#define MANTISSA_MASK (0x000FFFFFFFFFFFFFLL)
#define EXPONENT_MASK (0x7FF0000000000000LL)
#define NORMAL_MASK (0x0010000000000000LL)
#define SIGN_MASK (0x8000000000000000LL)
#else
#if defined(USE_L)
#define MANTISSA_MASK (0x000FFFFFFFFFFFFFL)
#define EXPONENT_MASK (0x7FF0000000000000L)
#define NORMAL_MASK (0x0010000000000000L)
#define SIGN_MASK (0x8000000000000000L)
#else
#define MANTISSA_MASK (0x000FFFFFFFFFFFFF)
#define EXPONENT_MASK (0x7FF0000000000000)
#define NORMAL_MASK (0x0010000000000000)
#define SIGN_MASK (0x8000000000000000)
#endif /* USE_L */
#endif /* USE_LL */

#define E_OFFSET (1075)

#define FLOAT_MANTISSA_MASK (0x007FFFFF)
#define FLOAT_EXPONENT_MASK (0x7F800000)
#define FLOAT_NORMAL_MASK   (0x00800000)
#define FLOAT_E_OFFSET (150)

IDATA
simpleAddHighPrecision (U_64 * arg1, IDATA length, U_64 arg2)
{
  /* assumes length > 0 */
  IDATA index = 1;

  *arg1 += arg2;
  if (arg2 <= *arg1)
    return 0;
  else if (length == 1)
    return 1;

  while (++arg1[index] == 0 && ++index < length);

  return (IDATA) index == length;
}

IDATA
addHighPrecision (U_64 * arg1, IDATA length1, U_64 * arg2, IDATA length2)
{
  /* addition is limited by length of arg1 as it this function is
   * storing the result in arg1 */
  /* fix for cc (GCC) 3.2 20020903 (Red Hat Linux 8.0 3.2-7): code generated does not
   * do the temp1 + temp2 + carry addition correct.  carry is 64 bit because gcc has
   * subtle issues when you mix 64 / 32 bit maths. */
  U_64 temp1, temp2, temp3;     /* temporary variables to help the SH-4, and gcc */
  U_64 carry;
  IDATA index;

  if (length1 == 0 || length2 == 0)
    {
      return 0;
    }
  else if (length1 < length2)
    {
      length2 = length1;
    }

  carry = 0;
  index = 0;
  do
    {
      temp1 = arg1[index];
      temp2 = arg2[index];
      temp3 = temp1 + temp2;
      arg1[index] = temp3 + carry;
      if (arg2[index] < arg1[index])
        carry = 0;
      else if (arg2[index] != arg1[index])
        carry = 1;
    }
  while (++index < length2);
  if (!carry)
    return 0;
  else if (index == length1)
    return 1;

  while (++arg1[index] == 0 && ++index < length1);

  return (IDATA) index == length1;
}

void
subtractHighPrecision (U_64 * arg1, IDATA length1, U_64 * arg2, IDATA length2)
{
  /* assumes arg1 > arg2 */
  IDATA index;
  for (index = 0; index < length1; ++index)
    arg1[index] = ~arg1[index];

  simpleAddHighPrecision (arg1, length1, 1);

  while (length2 > 0 && arg2[length2 - 1] == 0)
    --length2;

  addHighPrecision (arg1, length1, arg2, length2);

  for (index = 0; index < length1; ++index)
    arg1[index] = ~arg1[index];
  simpleAddHighPrecision (arg1, length1, 1);
}

U_32
simpleMultiplyHighPrecision (U_64 * arg1, IDATA length, U_64 arg2)
{
  /* assumes arg2 only holds 32 bits of information */
  U_64 product;
  IDATA index;

  index = 0;
  product = 0;

  do
    {
      product =
        HIGH_IN_U64 (product) + arg2 * LOW_U32_FROM_PTR (arg1 + index);
      LOW_U32_FROM_PTR (arg1 + index) = LOW_U32_FROM_VAR (product);
      product =
        HIGH_IN_U64 (product) + arg2 * HIGH_U32_FROM_PTR (arg1 + index);
      HIGH_U32_FROM_PTR (arg1 + index) = LOW_U32_FROM_VAR (product);
    }
  while (++index < length);

  return HIGH_U32_FROM_VAR (product);
}

void
simpleMultiplyAddHighPrecision (U_64 * arg1, IDATA length, U_64 arg2,
                                U_32 * result)
{
  /* Assumes result can hold the product and arg2 only holds 32 bits
     of information */
  U_64 product;
  IDATA index, resultIndex;

  index = resultIndex = 0;
  product = 0;

  do
    {
      product =
        HIGH_IN_U64 (product) + result[at (resultIndex)] +
        arg2 * LOW_U32_FROM_PTR (arg1 + index);
      result[at (resultIndex)] = LOW_U32_FROM_VAR (product);
      ++resultIndex;
      product =
        HIGH_IN_U64 (product) + result[at (resultIndex)] +
        arg2 * HIGH_U32_FROM_PTR (arg1 + index);
      result[at (resultIndex)] = LOW_U32_FROM_VAR (product);
      ++resultIndex;
    }
  while (++index < length);

  result[at (resultIndex)] += HIGH_U32_FROM_VAR (product);
  if (result[at (resultIndex)] < HIGH_U32_FROM_VAR (product))
    {
      /* must be careful with ++ operator and macro expansion */
      ++resultIndex;
      while (++result[at (resultIndex)] == 0)
        ++resultIndex;
    }
}

void
multiplyHighPrecision (U_64 * arg1, IDATA length1, U_64 * arg2, IDATA length2,
                       U_64 * result, IDATA length)
{
  /* assumes result is large enough to hold product */
  U_64 *temp;
  U_32 *resultIn32;
  IDATA count, index;

  if (length1 < length2)
    {
      temp = arg1;
      arg1 = arg2;
      arg2 = temp;
      count = length1;
      length1 = length2;
      length2 = count;
    }

  memset (result, 0, sizeof (U_64) * length);

  /* length1 > length2 */
  resultIn32 = (U_32 *) result;
  index = -1;
  for (count = 0; count < length2; ++count)
    {
      simpleMultiplyAddHighPrecision (arg1, length1, LOW_IN_U64 (arg2[count]),
                                      resultIn32 + (++index));
      simpleMultiplyAddHighPrecision (arg1, length1,
                                      HIGH_IN_U64 (arg2[count]),
                                      resultIn32 + (++index));

    }
}

U_32
simpleAppendDecimalDigitHighPrecision (U_64 * arg1, IDATA length, U_64 digit)
{
  /* assumes digit is less than 32 bits */
  U_64 arg;
  IDATA index = 0;

  digit <<= 32;
  do
    {
      arg = LOW_IN_U64 (arg1[index]);
      digit = HIGH_IN_U64 (digit) + TIMES_TEN (arg);
      LOW_U32_FROM_PTR (arg1 + index) = LOW_U32_FROM_VAR (digit);

      arg = HIGH_IN_U64 (arg1[index]);
      digit = HIGH_IN_U64 (digit) + TIMES_TEN (arg);
      HIGH_U32_FROM_PTR (arg1 + index) = LOW_U32_FROM_VAR (digit);
    }
  while (++index < length);

  return HIGH_U32_FROM_VAR (digit);
}

void
simpleShiftLeftHighPrecision (U_64 * arg1, IDATA length, IDATA arg2)
{
  /* assumes length > 0 */
  IDATA index, offset;
  if (arg2 >= 64)
    {
      offset = arg2 >> 6;
      index = length;

      while (--index - offset >= 0)
        arg1[index] = arg1[index - offset];
      do
        {
          arg1[index] = 0;
        }
      while (--index >= 0);

      arg2 &= 0x3F;
    }

  if (arg2 == 0)
    return;
  while (--length > 0)
    {
      arg1[length] = arg1[length] << arg2 | arg1[length - 1] >> (64 - arg2);
    }
  *arg1 <<= arg2;
}

IDATA
highestSetBit (U_64 * y)
{
  U_32 x;
  IDATA result;

  if (*y == 0)
    return 0;

#if defined(USE_LL)
  if (*y & 0xFFFFFFFF00000000LL)
    {
      x = HIGH_U32_FROM_PTR (y);
      result = 32;
    }
  else
    {
      x = LOW_U32_FROM_PTR (y);
      result = 0;
    }
#else
#if defined(USE_L)
  if (*y & 0xFFFFFFFF00000000L)
    {
      x = HIGH_U32_FROM_PTR (y);
      result = 32;
    }
  else
    {
      x = LOW_U32_FROM_PTR (y);
      result = 0;
    }
#else
  if (*y & 0xFFFFFFFF00000000)
    {
      x = HIGH_U32_FROM_PTR (y);
      result = 32;
    }
  else
    {
      x = LOW_U32_FROM_PTR (y);
      result = 0;
    }
#endif /* USE_L */
#endif /* USE_LL */

  if (x & 0xFFFF0000)
    {
      x = bitSection (x, 0xFFFF0000, 16);
      result += 16;
    }
  if (x & 0xFF00)
    {
      x = bitSection (x, 0xFF00, 8);
      result += 8;
    }
  if (x & 0xF0)
    {
      x = bitSection (x, 0xF0, 4);
      result += 4;
    }
  if (x > 0x7)
    return result + 4;
  else if (x > 0x3)
    return result + 3;
  else if (x > 0x1)
    return result + 2;
  else
    return result + 1;
}

IDATA
lowestSetBit (U_64 * y)
{
  U_32 x;
  IDATA result;

  if (*y == 0)
    return 0;

#if defined(USE_LL)
  if (*y & 0x00000000FFFFFFFFLL)
    {
      x = LOW_U32_FROM_PTR (y);
      result = 0;
    }
  else
    {
      x = HIGH_U32_FROM_PTR (y);
      result = 32;
    }
#else
#if defined(USE_L)
  if (*y & 0x00000000FFFFFFFFL)
    {
      x = LOW_U32_FROM_PTR (y);
      result = 0;
    }
  else
    {
      x = HIGH_U32_FROM_PTR (y);
      result = 32;
    }
#else
  if (*y & 0x00000000FFFFFFFF)
    {
      x = LOW_U32_FROM_PTR (y);
      result = 0;
    }
  else
    {
      x = HIGH_U32_FROM_PTR (y);
      result = 32;
    }
#endif /* USE_L */
#endif /* USE_LL */

  if (!(x & 0xFFFF))
    {
      x = bitSection (x, 0xFFFF0000, 16);
      result += 16;
    }
  if (!(x & 0xFF))
    {
      x = bitSection (x, 0xFF00, 8);
      result += 8;
    }
  if (!(x & 0xF))
    {
      x = bitSection (x, 0xF0, 4);
      result += 4;
    }

  if (x & 0x1)
    return result + 1;
  else if (x & 0x2)
    return result + 2;
  else if (x & 0x4)
    return result + 3;
  else
    return result + 4;
}

IDATA
highestSetBitHighPrecision (U_64 * arg, IDATA length)
{
  IDATA highBit;

  while (--length >= 0)
    {
      highBit = highestSetBit (arg + length);
      if (highBit)
        return highBit + 64 * length;
    }

  return 0;
}

IDATA
lowestSetBitHighPrecision (U_64 * arg, IDATA length)
{
  IDATA lowBit, index = -1;

  while (++index < length)
    {
      lowBit = lowestSetBit (arg + index);
      if (lowBit)
        return lowBit + 64 * index;
    }

  return 0;
}

IDATA
compareHighPrecision (U_64 * arg1, IDATA length1, U_64 * arg2, IDATA length2)
{
  while (--length1 >= 0 && arg1[length1] == 0);
  while (--length2 >= 0 && arg2[length2] == 0);

  if (length1 > length2)
    return 1;
  else if (length1 < length2)
    return -1;
  else if (length1 > -1)
    {
      do
        {
          if (arg1[length1] > arg2[length1])
            return 1;
          else if (arg1[length1] < arg2[length1])
            return -1;
        }
      while (--length1 >= 0);
    }

  return 0;
}

jdouble
toDoubleHighPrecision (U_64 * arg, IDATA length)
{
  IDATA highBit;
  U_64 mantissa, test64;
  U_32 test;
  jdouble result;

  while (length > 0 && arg[length - 1] == 0)
    --length;

  if (length == 0)
    result = 0.0;
  else if (length > 16)
    {
      DOUBLE_TO_LONGBITS (result) = EXPONENT_MASK;
    }
  else if (length == 1)
    {
      highBit = highestSetBit (arg);
      if (highBit <= 53)
        {
          highBit = 53 - highBit;
          mantissa = *arg << highBit;
          DOUBLE_TO_LONGBITS (result) =
            CREATE_DOUBLE_BITS (mantissa, -highBit);
        }
      else
        {
          highBit -= 53;
          mantissa = *arg >> highBit;
          DOUBLE_TO_LONGBITS (result) =
            CREATE_DOUBLE_BITS (mantissa, highBit);

          /* perform rounding, round to even in case of tie */
          test = (LOW_U32_FROM_PTR (arg) << (11 - highBit)) & 0x7FF;
          if (test > 0x400 || ((test == 0x400) && (mantissa & 1)))
            DOUBLE_TO_LONGBITS (result) = DOUBLE_TO_LONGBITS (result) + 1;
        }
    }
  else
    {
      highBit = highestSetBit (arg + (--length));
      if (highBit <= 53)
        {
          highBit = 53 - highBit;
          if (highBit > 0)
            {
              mantissa =
                (arg[length] << highBit) | (arg[length - 1] >>
                                            (64 - highBit));
            }
          else
            {
              mantissa = arg[length];
            }
          DOUBLE_TO_LONGBITS (result) =
            CREATE_DOUBLE_BITS (mantissa, length * 64 - highBit);

          /* perform rounding, round to even in case of tie */
          test64 = arg[--length] << highBit;
          if (test64 > SIGN_MASK || ((test64 == SIGN_MASK) && (mantissa & 1)))
            DOUBLE_TO_LONGBITS (result) = DOUBLE_TO_LONGBITS (result) + 1;
          else if (test64 == SIGN_MASK)
            {
              while (--length >= 0)
                {
                  if (arg[length] != 0)
                    {
                      DOUBLE_TO_LONGBITS (result) =
                        DOUBLE_TO_LONGBITS (result) + 1;
                      break;
                    }
                }
            }
        }
      else
        {
          highBit -= 53;
          mantissa = arg[length] >> highBit;
          DOUBLE_TO_LONGBITS (result) =
            CREATE_DOUBLE_BITS (mantissa, length * 64 + highBit);

          /* perform rounding, round to even in case of tie */
          test = (LOW_U32_FROM_PTR (arg + length) << (11 - highBit)) & 0x7FF;
          if (test > 0x400 || ((test == 0x400) && (mantissa & 1)))
            DOUBLE_TO_LONGBITS (result) = DOUBLE_TO_LONGBITS (result) + 1;
          else if (test == 0x400)
            {
              do
                {
                  if (arg[--length] != 0)
                    {
                      DOUBLE_TO_LONGBITS (result) =
                        DOUBLE_TO_LONGBITS (result) + 1;
                      break;
                    }
                }
              while (length > 0);
            }
        }
    }

  return result;
}

IDATA
tenToTheEHighPrecision (U_64 * result, IDATA length, jint e)
{
  /* size test */
  if (length < ((e / 19) + 1))
    return 0;

  memset (result, 0, length * sizeof (U_64));
  *result = 1;

  if (e == 0)
    return 1;

  length = 1;
  length = timesTenToTheEHighPrecision (result, length, e);
  /* bad O(n) way of doing it, but simple */
  /*
     do {
     overflow = simpleAppendDecimalDigitHighPrecision(result, length, 0);
     if (overflow)
     result[length++] = overflow;
     } while (--e);
   */
  return length;
}

IDATA
timesTenToTheEHighPrecision (U_64 * result, IDATA length, jint e)
{
  /* assumes result can hold value */
  U_64 overflow;
  int exp10 = e;

  if (e == 0)
    return length;

  /* bad O(n) way of doing it, but simple */
  /*
     do {
     overflow = simpleAppendDecimalDigitHighPrecision(result, length, 0);
     if (overflow)
     result[length++] = overflow;
     } while (--e);
   */
  /* Replace the current implementaion which performs a
   * "multiplication" by 10 e number of times with an actual
   * multiplication. 10e19 is the largest exponent to the power of ten
   * that will fit in a 64-bit integer, and 10e9 is the largest exponent to
   * the power of ten that will fit in a 64-bit integer. Not sure where the
   * break-even point is between an actual multiplication and a
   * simpleAappendDecimalDigit() so just pick 10e3 as that point for
   * now.
   */
  while (exp10 >= 19)
    {
      overflow = simpleMultiplyHighPrecision64 (result, length, TEN_E19);
      if (overflow)
        result[length++] = overflow;
      exp10 -= 19;
    }
  while (exp10 >= 9)
    {
      overflow = simpleMultiplyHighPrecision (result, length, TEN_E9);
      if (overflow)
        result[length++] = overflow;
      exp10 -= 9;
    }
  if (exp10 == 0)
    return length;
  else if (exp10 == 1)
    {
      overflow = simpleAppendDecimalDigitHighPrecision (result, length, 0);
      if (overflow)
        result[length++] = overflow;
    }
  else if (exp10 == 2)
    {
      overflow = simpleAppendDecimalDigitHighPrecision (result, length, 0);
      if (overflow)
        result[length++] = overflow;
      overflow = simpleAppendDecimalDigitHighPrecision (result, length, 0);
      if (overflow)
        result[length++] = overflow;
    }
  else if (exp10 == 3)
    {
      overflow = simpleMultiplyHighPrecision (result, length, TEN_E3);
      if (overflow)
        result[length++] = overflow;
    }
  else if (exp10 == 4)
    {
      overflow = simpleMultiplyHighPrecision (result, length, TEN_E4);
      if (overflow)
        result[length++] = overflow;
    }
  else if (exp10 == 5)
    {
      overflow = simpleMultiplyHighPrecision (result, length, TEN_E5);
      if (overflow)
        result[length++] = overflow;
    }
  else if (exp10 == 6)
    {
      overflow = simpleMultiplyHighPrecision (result, length, TEN_E6);
      if (overflow)
        result[length++] = overflow;
    }
  else if (exp10 == 7)
    {
      overflow = simpleMultiplyHighPrecision (result, length, TEN_E7);
      if (overflow)
        result[length++] = overflow;
    }
  else if (exp10 == 8)
    {
      overflow = simpleMultiplyHighPrecision (result, length, TEN_E8);
      if (overflow)
        result[length++] = overflow;
    }
  return length;
}

U_64
doubleMantissa (jdouble z)
{
  U_64 m = DOUBLE_TO_LONGBITS (z);

  if ((m & EXPONENT_MASK) != 0)
    m = (m & MANTISSA_MASK) | NORMAL_MASK;
  else
    m = (m & MANTISSA_MASK);

  return m;
}

IDATA
doubleExponent (jdouble z)
{
  /* assumes positive double */
  IDATA k = HIGH_U32_FROM_VAR (z) >> 20;

  if (k)
    k -= E_OFFSET;
  else
    k = 1 - E_OFFSET;

  return k;
}

UDATA
floatMantissa (jfloat z)
{
  UDATA m = (UDATA) FLOAT_TO_INTBITS (z);

  if ((m & FLOAT_EXPONENT_MASK) != 0)
    m = (m & FLOAT_MANTISSA_MASK) | FLOAT_NORMAL_MASK;
  else
    m = (m & FLOAT_MANTISSA_MASK);

  return m;
}

IDATA
floatExponent (jfloat z)
{
  /* assumes positive float */
  IDATA k = FLOAT_TO_INTBITS (z) >> 23;
  if (k)
    k -= FLOAT_E_OFFSET;
  else
    k = 1 - FLOAT_E_OFFSET;

  return k;
}

/* Allow a 64-bit value in arg2 */
U_64
simpleMultiplyHighPrecision64 (U_64 * arg1, IDATA length, U_64 arg2)
{
  U_64 intermediate, *pArg1, carry1, carry2, prod1, prod2, sum;
  IDATA index;
  U_32 buf32;

  index = 0;
  intermediate = 0;
  pArg1 = arg1 + index;
  carry1 = carry2 = 0;

  do
    {
      if ((*pArg1 != 0) || (intermediate != 0))
        {
          prod1 =
            (U_64) LOW_U32_FROM_VAR (arg2) * (U_64) LOW_U32_FROM_PTR (pArg1);
          sum = intermediate + prod1;
          if ((sum < prod1) || (sum < intermediate))
            {
              carry1 = 1;
            }
          else
            {
              carry1 = 0;
            }
          prod1 =
            (U_64) LOW_U32_FROM_VAR (arg2) * (U_64) HIGH_U32_FROM_PTR (pArg1);
          prod2 =
            (U_64) HIGH_U32_FROM_VAR (arg2) * (U_64) LOW_U32_FROM_PTR (pArg1);
          intermediate = carry2 + HIGH_IN_U64 (sum) + prod1 + prod2;
          if ((intermediate < prod1) || (intermediate < prod2))
            {
              carry2 = 1;
            }
          else
            {
              carry2 = 0;
            }
          LOW_U32_FROM_PTR (pArg1) = LOW_U32_FROM_VAR (sum);
          buf32 = HIGH_U32_FROM_PTR (pArg1);
          HIGH_U32_FROM_PTR (pArg1) = LOW_U32_FROM_VAR (intermediate);
          intermediate = carry1 + HIGH_IN_U64 (intermediate)
            + (U_64) HIGH_U32_FROM_VAR (arg2) * (U_64) buf32;
        }
      pArg1++;
    }
  while (++index < length);
  return intermediate;
}
/* cbigint.c } */

/* org_apache_harmony_luni_util_NumberConvert.c { */
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#include <string.h>
#include <math.h>
#include <stdlib.h>

#if defined(LINUX) || defined(FREEBSD)
#define USE_LL
#endif

#define INV_LOG_OF_TEN_BASE_2 (0.30102999566398114) /* Local */
#define ERROR_OCCURED(x) (HIGH_I32_FROM_VAR(x) < 0) /* Local */

/*NB the Number converter methods are synchronized so it is possible to
 *have global data for use by bigIntDigitGenerator */
#define RM_SIZE 21     /* Local. */
#define STemp_SIZE 22  /* Local. */

#if defined(WIN32)
#pragma optimize("",on)         /*restore optimizations */
#endif

/* The algorithm for this particular function can be found in:
 *
 *      Printing Floating-Point Numbers Quickly and Accurately, Robert
 *      G. Burger, and R. Kent Dybvig, Programming Language Design and
 *      Implementation (PLDI) 1996, pp.108-116.
 *
 * The previous implementation of this function combined m+ and m- into
 * one single M which caused some inaccuracy of the last digit. The
 * particular case below shows this inaccuracy:
 *
 *       System.out.println(new Double((1.234123412431233E107)).toString());
 *       System.out.println(new Double((1.2341234124312331E107)).toString());
 *       System.out.println(new Double((1.2341234124312332E107)).toString());
 *
 *       outputs the following:
 *
 *           1.234123412431233E107
 *           1.234123412431233E107
 *           1.234123412431233E107
 *
 *       instead of:
 *
 *           1.234123412431233E107
 *           1.2341234124312331E107
 *           1.2341234124312331E107
 * 
 */
static void bigIntDigitGeneratorInstImpl (
  NumberConverter *ncp,
  jlong f,
  jint e,
  jboolean isDenormalized,
  jboolean mantissaIsZero,
  jint p)

{
  int RLength, SLength, TempLength, mplus_Length, mminus_Length;
  int high, low, i;
  jint k, firstK, U;
  jint getCount, setCount;
  jint *uArray;

  U_64 R[RM_SIZE], S[STemp_SIZE], mplus[RM_SIZE], mminus[RM_SIZE],
    Temp[STemp_SIZE];

  memset (R     , 0, RM_SIZE    * sizeof (U_64));
  memset (S     , 0, STemp_SIZE * sizeof (U_64));
  memset (mplus , 0, RM_SIZE    * sizeof (U_64));
  memset (mminus, 0, RM_SIZE    * sizeof (U_64));
  memset (Temp  , 0, STemp_SIZE * sizeof (U_64));

  if (e >= 0)
    {
      *R = f;
      *mplus = *mminus = 1;
      simpleShiftLeftHighPrecision (mminus, RM_SIZE, e);
      if (f != (2 << (p - 1)))
        {
          simpleShiftLeftHighPrecision (R, RM_SIZE, e + 1);
          *S = 2;
          /*
           * m+ = m+ << e results in 1.0e23 to be printed as
           * 0.9999999999999999E23
           * m+ = m+ << e+1 results in 1.0e23 to be printed as
           * 1.0e23 (caused too much rounding)
           *      470fffffffffffff = 2.0769187434139308E34
           *      4710000000000000 = 2.076918743413931E34
           */
          simpleShiftLeftHighPrecision (mplus, RM_SIZE, e);
        }
      else
        {
          simpleShiftLeftHighPrecision (R, RM_SIZE, e + 2);
          *S = 4;
          simpleShiftLeftHighPrecision (mplus, RM_SIZE, e + 1);
        }
    }
  else
    {
      if (isDenormalized || (f != (2 << (p - 1))))
        {
          *R = f << 1;
          *S = 1;
          simpleShiftLeftHighPrecision (S, STemp_SIZE, 1 - e);
          *mplus = *mminus = 1;
        }
      else
        {
          *R = f << 2;
          *S = 1;
          simpleShiftLeftHighPrecision (S, STemp_SIZE, 2 - e);
          *mplus = 2;
          *mminus = 1;
        }
    }

  k = (int) ceil ((e + p - 1) * INV_LOG_OF_TEN_BASE_2 - 1e-10);

  if (k > 0)
    {
      timesTenToTheEHighPrecision (S, STemp_SIZE, k);
    }
  else
    {
      timesTenToTheEHighPrecision (R     , RM_SIZE, -k);
      timesTenToTheEHighPrecision (mplus , RM_SIZE, -k);
      timesTenToTheEHighPrecision (mminus, RM_SIZE, -k);
    }

  RLength = mplus_Length = mminus_Length = RM_SIZE;
  SLength = TempLength = STemp_SIZE;

  memset (Temp + RM_SIZE, 0, (STemp_SIZE - RM_SIZE) * sizeof (U_64));
  memcpy (Temp, R, RM_SIZE * sizeof (U_64));

  while (RLength > 1 && R[RLength - 1] == 0)
    --RLength;
  while (mplus_Length > 1 && mplus[mplus_Length - 1] == 0)
    --mplus_Length;
  while (mminus_Length > 1 && mminus[mminus_Length - 1] == 0)
    --mminus_Length;
  while (SLength > 1 && S[SLength - 1] == 0)
    --SLength;
  TempLength = (RLength > mplus_Length ? RLength : mplus_Length) + 1;
  addHighPrecision (Temp, TempLength, mplus, mplus_Length);

  if (compareHighPrecision (Temp, TempLength, S, SLength) >= 0)
    {
      firstK = k;
    }
  else
    {
      firstK = k - 1;
      simpleAppendDecimalDigitHighPrecision (R     , ++RLength      , 0);
      simpleAppendDecimalDigitHighPrecision (mplus , ++mplus_Length , 0);
      simpleAppendDecimalDigitHighPrecision (mminus, ++mminus_Length, 0);
      while (RLength > 1 && R[RLength - 1] == 0)
        --RLength;
      while (mplus_Length > 1 && mplus[mplus_Length - 1] == 0)
        --mplus_Length;
      while (mminus_Length > 1 && mminus[mminus_Length - 1] == 0)
        --mminus_Length;
    }

  uArray = ncp->uArray;

  getCount = setCount = 0;
  do
    {
      U = 0;
      for (i = 3; i >= 0; --i)
        {
          TempLength = SLength + 1;
          Temp[SLength] = 0;
          memcpy (Temp, S, SLength * sizeof (U_64));
          simpleShiftLeftHighPrecision (Temp, TempLength, i);
          if (compareHighPrecision (R, RLength, Temp, TempLength) >= 0)
            {
              subtractHighPrecision (R, RLength, Temp, TempLength);
              U += 1 << i;
            }
        }

      low = compareHighPrecision (R, RLength, mminus, mminus_Length) <= 0;

      memset (Temp + RLength, 0, (STemp_SIZE - RLength) * sizeof (U_64));
      memcpy (Temp, R, RLength * sizeof (U_64));
      TempLength = (RLength > mplus_Length ? RLength : mplus_Length) + 1;
      addHighPrecision (Temp, TempLength, mplus, mplus_Length);

      high = compareHighPrecision (Temp, TempLength, S, SLength) >= 0;

      if (low || high)
        break;

      simpleAppendDecimalDigitHighPrecision (R     , ++RLength      , 0);
      simpleAppendDecimalDigitHighPrecision (mplus , ++mplus_Length , 0);
      simpleAppendDecimalDigitHighPrecision (mminus, ++mminus_Length, 0);
      while (RLength > 1 && R[RLength - 1] == 0)
        --RLength;
      while (mplus_Length > 1 && mplus[mplus_Length - 1] == 0)
        --mplus_Length;
      while (mminus_Length > 1 && mminus[mminus_Length - 1] == 0)
        --mminus_Length;
      uArray[setCount++] = U;
    }
  while (1);

  simpleShiftLeftHighPrecision (R, ++RLength, 1);
  if (low && !high)
    uArray[setCount++] = U;
  else if (high && !low)
    uArray[setCount++] = U + 1;
  else if (compareHighPrecision (R, RLength, S, SLength) < 0)
    uArray[setCount++] = U;
  else
    uArray[setCount++] = U + 1;

  ncp->setCount = setCount;
  ncp->getCount = getCount;
  ncp->firstK = firstK;

}

/* org_apache_harmony_luni_util_NumberConvert.c } */

