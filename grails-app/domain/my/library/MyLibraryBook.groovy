package my.library

import grails.compiler.GrailsCompileStatic
import taack.ast.annotation.TaackFieldEnum

@GrailsCompileStatic
@TaackFieldEnum
class MyLibraryBook {
    String title
    MyLibraryAuthor author
    String description
    int numberOfPages
    List<MyLibraryBookInstance> listOfBookInstance
    int count = 0

    static constraints = {}

    static hasMany = [listOfBookInstance: MyLibraryBookInstance]

    int getNumberOfInstances() {
        if (!listOfBookInstance) { return 0}
        count = listOfBookInstance.count {it.isActive} as int
        return count
    }
}
