/*
 *  boot.c: Main program for CSE minijava compiled code
 *          Ruth Anderson & Hal Perkins
 *
 *  Modified 11/11, 2/15 for 64-bit code
 *
 *  Contents:
 *    Main program that calls the compiled code as a function
 *    Function put that can be used by compiled code for integer output
 *    Function mjcalloc to allocate zeroed bytes for minijava's new operator
 *
 *  Additional functions used by compiled code can be added as desired.
 */

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <inttypes.h>

#include "number_converter.h"

#define ERROR_MJCALLOC 101

extern void asm_main();   /* main function in compiled code */
                          /* change function name if your   */
                          /* compiled main has a different label */

/* Write x to standard output followed by a newline */
void put(int64_t x) {
  printf("%" PRId64 "\n", x);
}

void putdub(double x) {
  char buf[1024];
  convert_double(x, buf, 1024);
  printf("%s\n", buf);
}

void check_pos_num(int n) {
  if (n < 0) {
    exit(1);
  }
}

void check_arr_idx(int i, int len) {
  if (i < 0 || i >= len) {
    printf("ArrayIndexOutOfBoundsException\n");
    exit(0);
  }
}

/*
 *  mjcalloc returns a pointer to a chunk of memory with at least
 *  num_bytes available.  Returned storage has been zeroed out.
 *  Crashes if the memory allocation failed.
 */

void* mjcalloc(size_t num_bytes) {
  void* obj = calloc(1, num_bytes);

  if (obj == NULL) {
    printf("Failed to allocate memory for new object\n");
    exit(ERROR_MJCALLOC);
  }

  return obj;
}

/* Execute compiled program asm_main */
int main() {
  asm_main();
  return 0;
}
