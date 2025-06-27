package my.library

import crew.CrewController
import crew.User
import grails.plugin.springsecurity.SpringSecurityService
import jakarta.annotation.PostConstruct
import org.codehaus.groovy.runtime.MethodClosure
import taack.app.TaackApp
import taack.app.TaackAppRegisterService
import taack.render.TaackUiEnablerService

class MyLibrarySecurityService {
    SpringSecurityService springSecurityService

    static lazyInit = false

    private securityClosure(Long id, Map p) {
        if (!id && !p) return true
        if (!id) return true
        canEdit(User.read(id))
    }

    User authenticatedRolesUser() {
        springSecurityService.currentUser as User
    }

    boolean authenticatedRoles(String... roles) {
        User user = authenticatedRolesUser()
        for (String r : roles) {
            if (user.authorities*.authority.contains(r)) return true
        }
        return false
    }


    boolean isAdmin() {
        authenticatedRoles('ROLE_ADMIN')
    }

    boolean canEdit(User target) {
        admin
    }



    private static boolean dateSecurityClosure(Long id, Map p) {
        MyLibraryBorrowed borrowed = MyLibraryBorrowed.get(id)
//        println("-------------------------------------------------------------")
//        println(borrowed.approvalDate)
        return (borrowed.statusOfApproval == ApprovalStatus.APPROVED)//is null -> false
    }


    private static boolean approvedSecurityClosure(Long id, Map p) {
        MyLibraryBorrowed borrowed = MyLibraryBorrowed.get(id)
//        println("-------------------------------------------------------------")
//        println(borrowed.approvalDate)
        return (borrowed.statusOfApproval != ApprovalStatus.APPROVED)//is null -> false
    }

    @PostConstruct
    void init() {
        TaackUiEnablerService.securityClosure(
                this.&securityClosure,
                MyLibraryController.&saveAuthor as MethodClosure,
                MyLibraryController.&deleteAuthor as MethodClosure,
                MyLibraryController.&activateAuthor as MethodClosure,
                MyLibraryController.&saveBook as MethodClosure,
                MyLibraryController.&deleteBook as MethodClosure,
                MyLibraryController.&bookAdd as MethodClosure,
                MyLibraryController.&purchaseAndSaveBook as MethodClosure,
                MyLibraryController.&deleteBookInstances as MethodClosure,
                MyLibraryController.&saveApprovalBookForm as MethodClosure,
                MyLibraryController.&deleteUser as MethodClosure)
//        )

        TaackUiEnablerService.securityClosure(
                this.&dateSecurityClosure,
                MyLibraryController.&returnBook as MethodClosure)

        TaackUiEnablerService.securityClosure(
                this.&approvedSecurityClosure,
                MyLibraryController.&approveBook as MethodClosure)


        TaackAppRegisterService.register(new TaackApp(MyLibraryController.&index as MethodClosure, new String(this.class.getResourceAsStream("/myLibrary/library-svgrepo-com.svg").readAllBytes())))

    }



}
