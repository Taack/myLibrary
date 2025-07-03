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

    List<MyLibraryBorrowed> borrowHistoryOfBook
    Boolean isAvailableB = true

    static constraints = {}

    static belongsTo = [book: MyLibraryBook]
    static hasMany = [borrowHistoryOfBook: MyLibraryBorrowed]
}
