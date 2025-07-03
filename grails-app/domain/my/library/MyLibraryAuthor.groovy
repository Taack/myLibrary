package my.library

import grails.compiler.GrailsCompileStatic
import taack.ast.annotation.TaackFieldEnum

/**
 * Domain class representing an *Author* entity in the library system.
 *
 * **Purpose:** Stores author information and links to their books.
 *
 * **To implement:**
 * - Fields:
 *   - firstName (String)
 *   - lastName (String)
 *   - dateOfBirth (Date)
 *   - listOfBooks (List<MyLibraryBook>), declare and initialize this list to an empty list
 *   - isActive (Boolean, default true)
 * - Relationship: hasMany listOfBooks (List<MyLibraryBook>)
 * - Method: toString() returning 'firstName lastName'
 */
@GrailsCompileStatic
@TaackFieldEnum
class MyLibraryAuthor {
    String firstName
    String lastName
    Date dateOfBirth
    List<MyLibraryBook> listOfBooks
    Boolean isActive = true

    static constraints = {}

    static hasMany = [listOfBooks: MyLibraryBook]

    String toString() {
        return firstName + ' ' + lastName
    }
}