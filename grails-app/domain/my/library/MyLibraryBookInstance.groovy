package my.library

import grails.compiler.GrailsCompileStatic
import taack.ast.annotation.TaackFieldEnum

/**
 * Domain class representing a *physical copy (instance)* of a book in the library system.
 *
 * **Purpose:** Stores information about each individual copy of a book.
 *
 * **To implement:**
 * - Fields:
 *   - book (MyLibraryBook): reference to the parent book.
 *   - isActive (Boolean, default true): indicates if this copy is active/available.
 *   - serialNumber (Integer): unique identifier for the book instance, randomly generated.
 * - Relationship: belongsTo book (MyLibraryBook), to define ownership.
 *
 * **Tip:** Initialize serialNumber with a random value to simulate unique instance IDs, you can use new Random().nextInt(100000)
 */
@GrailsCompileStatic
@TaackFieldEnum
class MyLibraryBookInstance {
    MyLibraryBook book
    Boolean isActive = true
    Integer serialNumber = new Random().nextInt(100000) //(1)

    static constraints = {}

    static belongsTo = [book: MyLibraryBook] //(2)

}