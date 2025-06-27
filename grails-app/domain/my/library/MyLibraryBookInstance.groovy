package my.library

import grails.compiler.GrailsCompileStatic
import taack.ast.annotation.TaackFieldEnum


@GrailsCompileStatic
@TaackFieldEnum
class MyLibraryBookInstance {
    MyLibraryBook book
    List<MyLibraryBorrowed> borrowHistoryOfBook
    Boolean isActive = true
    Integer serialNumber = new Random().nextInt(100000)
    Boolean isAvailableB = true

    static constraints = {}

    static belongsTo = [book: MyLibraryBook]
    static hasMany = [borrowHistoryOfBook: MyLibraryBorrowed]

    def getIsAvailable() {
        if(borrowHistoryOfBook == null || borrowHistoryOfBook.isEmpty()) {
            isAvailableB = true
            return true
        }
        else if (borrowHistoryOfBook[-1].returnDate != null) {
            isAvailableB = true
            return true
        } isAvailableB = false
        return false
    }


}
