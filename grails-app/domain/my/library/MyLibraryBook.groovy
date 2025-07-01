package my.library

import grails.compiler.GrailsCompileStatic
import taack.ast.annotation.TaackFieldEnum

/**
 * Domain class representing a *Book* entity in the library system.
 *
 * **Purpose:** Stores book details and links to its physical instances.
 *
 * **To implement:**
 * - Fields:
 *   - title (String)
 *   - author (MyLibraryAuthor)
 *   - description (String)
 *   - numberOfPages (int)
 *   - listOfBookInstance (List<MyLibraryBookInstance>), declare and initialize this list
 *   - count (int, default 0) to store the active instances count
 * - Relationship: hasMany listOfBookInstance (List<MyLibraryBookInstance>)
 * - Method:
 *   - getNumberOfInstances(): returns the number of active book instances (where isActive is true).
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
}