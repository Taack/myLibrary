package my.library

import grails.compiler.GrailsCompileStatic
import taack.ast.annotation.TaackFieldEnum

/**
 * Domain class representing a *physical copy (instance)* of a book in the library system.
 *
 * **Purpose:** Stores information about each individual copy of a book.
 *
 * **To implement (TODO 1.1 for added elements only):**
 * - Fields:
 *   - borrowHistoryOfBook (List<MyLibraryBorrowed>): the borrowing history of this book instance.
 *   - isAvailableB (Boolean, default true): indicates if this copy is currently available.
 * - Relationship:
 *   - hasMany borrowHistoryOfBook (MyLibraryBorrowed), to store borrowing records.
 * - Method:
 *   - getIsAvailable(): returns true if the book is available (no borrow history or last borrow has a return date), false otherwise.
 */
@GrailsCompileStatic
@TaackFieldEnum
class MyLibraryBookInstance {
    MyLibraryBook book
    Boolean isActive = true
    Integer serialNumber = new Random().nextInt(100000)

    // TODO 1.2.1: Define field borrowHistoryOfBook as List<MyLibraryBorrowed>.
    // TODO 1.2.2: Define field isAvailableB as Boolean, default true.

    static constraints = {}

    static belongsTo = [book: MyLibraryBook]

    // TODO 1.2.3: Define hasMany relationship for borrowHistoryOfBook (MyLibraryBorrowed).
}
