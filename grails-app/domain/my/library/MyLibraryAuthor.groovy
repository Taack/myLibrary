package my.library

import grails.compiler.GrailsCompileStatic
import taack.ast.annotation.TaackFieldEnum

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
