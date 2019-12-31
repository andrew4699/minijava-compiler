# Testing

This subdirectory contains the beginnings of a test suite that you can expand
upon to test your compiler.  Currently, a single test case is provided for
demonstration purposes that tests the small demo scanner.  *Note that this
test case is NOT intended to work with your implementation of the MiniJava
scanner!*  It uses the simple demo language (see `README.md` in the project root),
so the tokens may not be the same as in MiniJava.

These tests exist as a tool for you to check your work on the MiniJava compiler.
It is very difficult to be thorough about edge cases when testing manually, so
you are strongly encouraged to spend time coming up with test cases for each
phase of the compiler project. 

## Directory structure

We've provided this directory structure as a
suggestion of how to do that, but you are welcome to modify/replace anything as
you see fit.  The suggested test format (if you decide to use it) is organized
into two subdirectories:

    junit:     contains JUnit tests.  One option is to group these tests by
               portion of the project, but you may find a different
               organization you like better.

    resources: contains input (*.java) and expected output (*.expected) files
               for the JUnit tests.

               Note that the distinction between this and junit/ is significant:
               since we are using .java files as input to the compiler, we need
               to separate them from the real Java source files that contain
               test cases.  One option is to group these in folders
               corresponding to each portion of the project, but you may find
               a different organization you like better.

## Running tests

Tests can be compiled and run using ant.  The provided `build.xml` file comes with
a `compile-test` task that simply compiles any tests contained in `test/junit`,
and a `test` task that runs all those tests.  You may want to modify or expand
upon those tasks as you develop your compiler, especially if you choose to
change the organization of this directory.

## Writing tests

Finally, here are some ideas of how you might want to expand this testing
setup as you develop your compiler:

- Run tests on the main method in `MiniJava.java` instead of the underlying
  components (especially starting with Semantics).
- In addition to testing for cases that should work, try writing tests for
  cases that are supposed to fail.
- For the code generation portion, write tests that automatically compare
  the behavior of your generated code with the behavior produced by `javac`.