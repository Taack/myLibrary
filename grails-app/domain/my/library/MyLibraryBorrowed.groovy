package my.library

import crew.User
import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.SpringSecurityService
import taack.ast.annotation.TaackFieldEnum

enum ApprovalStatus {
    PENDING, APPROVED, REJECTED
}

@GrailsCompileStatic
@TaackFieldEnum
class MyLibraryBorrowed {
//    SpringSecurityService springSecurityService

    MyLibraryBookInstance bookInstance
    User user
    Date requestDate
    Date approvalDate
    Date returnDate
    ApprovalStatus statusOfApproval= ApprovalStatus.PENDING

//    def getCurrentUser() {
//        return springSecurityService.getCurrentUser() as User
//    }

    static constraints = {
        approvalDate nullable: true, validator: { Date val, MyLibraryBorrowed obj ->
            if (val == null) return true
            return val >= obj.requestDate
        }

        returnDate nullable: true, validator: { Date val, MyLibraryBorrowed obj ->
            if (val == null) return true
            return val >= obj.approvalDate
        }

        bookInstance nullable:false
        user nullable:false

    }


}

