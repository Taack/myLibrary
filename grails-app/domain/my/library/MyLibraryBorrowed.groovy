package my.library

import crew.User
import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.SpringSecurityService
import taack.ast.annotation.TaackFieldEnum

/**
 * Represents a borrow record for a book instance in the library system.
 *
 * **Purpose:** Stores all information related to the borrowing of a specific book instance by a user, including approval status and dates.
 *
 * **Fields:**
 * - `bookInstance` (MyLibraryBookInstance): The specific physical copy of the book being borrowed.
 * - `user` (User): The user who requested and/or borrowed the book.
 * - `requestDate` (Date): The date when the borrow request was made.
 * - `approvalDate` (Date): The date when the borrow request was approved.
 * - `returnDate` (Date): The date when the book was returned.
 * - `statusOfApproval` (ApprovalStatus, default = PENDING): The approval status of the borrow request (PENDING, APPROVED, or REJECTED).
 *
 * **Relationships:**
 * - Uses the `ApprovalStatus` enum to define possible approval states.
 *
 * **Constraints:**
 * - approvalDate and returnDate validations ensure chronological consistency.
 * - bookInstance and user cannot be null.
 */
enum ApprovalStatus {
    PENDING, APPROVED, REJECTED
}

@GrailsCompileStatic
@TaackFieldEnum
class MyLibraryBorrowed {
    MyLibraryBookInstance bookInstance
    User user
    Date requestDate
    Date approvalDate
    Date returnDate
    ApprovalStatus statusOfApproval = ApprovalStatus.PENDING

    static constraints = {
        // TODO 1.1: Add validation so approvalDate is nullable and, if provided, must be after or equal to requestDate.
        // TODO 1.2: Add validation so returnDate is nullable and, if provided, must be after or equal to approvalDate.
        // TODO 1.3: Ensure bookInstance is not nullable.
        // TODO 1.4: Ensure user is not nullable.
    }
}
