package my.library

import grails.compiler.GrailsCompileStatic
import taack.ast.annotation.TaackFieldEnum


@GrailsCompileStatic
@TaackFieldEnum
class MyLibraryBookInstance {
    MyLibraryBook book
    Boolean isActive = true
    Integer serialNumber = new Random().nextInt(100000)

    static constraints = {}

    static belongsTo = [book: MyLibraryBook]
}
