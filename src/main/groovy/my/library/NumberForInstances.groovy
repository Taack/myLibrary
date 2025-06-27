package my.library

import grails.compiler.GrailsCompileStatic
import grails.validation.Validateable
import taack.ast.annotation.TaackFieldEnum

@TaackFieldEnum
@GrailsCompileStatic
class NumberForInstances implements Validateable {
    Integer numberOfInstances
}