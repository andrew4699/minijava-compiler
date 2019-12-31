This subdirectory contains the declarations of classes in the AST
representation of a program in our simple demo language, based off
of the AST classes found on the MiniJava website.

These have been
changed to also keep track of the locations in the original source
file on which the AST element was found, as well as adding a separate
`Display` node only used in the toy language, but not for MiniJava.
Additional minor changes to replace `Vector` with `List`/`ArrayList` and
use type parameters where appropriate.
