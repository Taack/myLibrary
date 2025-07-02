package my.library

import grails.compiler.GrailsCompileStatic
import taack.ast.annotation.TaackFieldEnum

/**
 * Domain class representing a *Book* entity in the library system.
 *
 * **Purpose:** Stores book details and links to its physical instances.
 *
 * **To implement (TODO 1.2 for added elements only):**
 * - Method:
 *   - getNumberOfBooksBorrowable(): returns the count of available and active book instances for this book.
 */
@GrailsCompileStatic
@TaackFieldEnum
class MyLibraryBook {
    String title
    MyLibraryAuthor author
    String description
    int numberOfPages
    List<MyLibraryBookInstance> listOfBookInstance //(1)
    int count = 0

    static constraints = {}

    static hasMany = [listOfBookInstance: MyLibraryBookInstance]

    int getNumberOfInstances() {
        if (!listOfBookInstance) { return 0}
        count = listOfBookInstance.count {it.isActive} as int
        return count
    }

    // TODO 1.1.1: Implement getNumberOfBooksBorrowable() to return the count of MyLibraryBookInstance where:
    // - isAvailableB is true
    // - isActive is true
    // - book equals this book instance
}
