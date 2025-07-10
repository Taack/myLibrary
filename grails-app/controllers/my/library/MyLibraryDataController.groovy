package my.library

import crew.Role
import crew.User
import crew.UserRole
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId

/**
 * == MyLibraryDataController
 *
 * This controller is secured to 'ROLE_ADMIN' and is responsible for creating
 * all initial data for the MyLibrary application, including:
 *
 * - Admin and borrower users
 * - Authors
 * - Books
 * - Book instances
 * - Borrowed entries (borrowing history)
 *
 * === Data Generation
 *
 * The data is created in a structured sequence:
 *
 * .createDataUsers() creates roles, users, and user-role associations.
 * .createDataAuthorsBooks() creates authors and books.
 * .createDataBookInstances() creates physical book instances per book.
 * .createDataBorrowed() creates borrowing records for each instance.
 *
 * Note: The methods are annotated with @Transactional to ensure database consistency.
 *
 * === Example usage
 *
 * You can trigger data generation via the controller route mapped to createDataUsers.
 * This will populate the database with realistic sample data for testing or demos.
 *
 * === Further Reading
 *
 * For more information on GORM data creation and transactions, see:
 *
 * - GORM Quick Reference: https://gorm.grails.org/latest/hibernate/manual/index.html#saving
 * - Grails Controllers: https://docs.grails.org/latest/guide/theWebLayer.html#controllers
 *
 * === Author
 *
 * Generated and maintained by the MyLibrary team.
 */
@Secured(['ROLE_ADMIN'])
class MyLibraryDataController {
    Random random = new Random(42)

    /**
     * Creates admin and borrower users, their roles, and triggers
     * subsequent data creation for authors, books, instances, and borrowed entries.
     *
     * Calls:
     * - createDataAuthorsBooks()
     *
     * @Transactional ensures all inserts are committed together.
     */
    @Transactional
    def createDataUsers() {

        // === 1. Create Roles ===
        Role borrowerRole = Role.findByAuthority('ROLE_BORROWER') ?: new Role(authority: "ROLE_BORROWER")
        borrowerRole.save(flush:true)
        Role adminRole = Role.findByAuthority('ROLE_ADMIN') ?: new Role(authority: "ROLE_ADMIN")
        adminRole.save(flush:true)

        // === 2. Define unique realistic user data ===
        List<Map> adminUsers = [
                [firstName: 'John', lastName: 'Doe', username: 'jdoe'],
                [firstName: 'Alice', lastName: 'Smith', username: 'asmith'],
                [firstName: 'Brian', lastName: 'Williams', username: 'bwilliams']
        ]

        List<Map> borrowerUsers = [
                [firstName: 'Emily', lastName: 'Johnson', username: 'ejohnson'], [firstName: 'Michael', lastName: 'Brown', username: 'mbrown'],
                [firstName: 'Jessica', lastName: 'Davis', username: 'jdavis'], [firstName: 'David', lastName: 'Miller', username: 'dmiller'],
                [firstName: 'Sarah', lastName: 'Wilson', username: 'swilson'], [firstName: 'Daniel', lastName: 'Moore', username: 'dmoore'],
                [firstName: 'Laura', lastName: 'Taylor', username: 'ltaylor'], [firstName: 'James', lastName: 'Anderson', username: 'janderson'],
                [firstName: 'Olivia', lastName: 'Thomas', username: 'othomas'], [firstName: 'Matthew', lastName: 'Jackson', username: 'mjackson'],
                [firstName: 'Sophia', lastName: 'White', username: 'swhite'], [firstName: 'Anthony', lastName: 'Harris', username: 'aharris'],
                [firstName: 'Isabella', lastName: 'Martin', username: 'imartin'], [firstName: 'Andrew', lastName: 'Thompson', username: 'athompson'],
                [firstName: 'Megan', lastName: 'Garcia', username: 'mgarcia'], [firstName: 'Joshua', lastName: 'Martinez', username: 'jmartinez'],
                [firstName: 'Grace', lastName: 'Robinson', username: 'grobinson'], [firstName: 'Christopher', lastName: 'Clark', username: 'cclark'],
                [firstName: 'Natalie', lastName: 'Rodriguez', username: 'nrodriguez'], [firstName: 'Brandon', lastName: 'Lewis', username: 'blewis'],

                [firstName: 'Ashley', lastName: 'Walker', username: 'awalker'], [firstName: 'Ryan', lastName: 'Hall', username: 'rhall'],
                [firstName: 'Abigail', lastName: 'Allen', username: 'aallen'], [firstName: 'Justin', lastName: 'Young', username: 'jyoung'],
                [firstName: 'Samantha', lastName: 'Hernandez', username: 'shernandez'], [firstName: 'Jacob', lastName: 'King', username: 'jking'],
                [firstName: 'Madison', lastName: 'Wright', username: 'mwright'], [firstName: 'Ethan', lastName: 'Lopez', username: 'elopez'],
                [firstName: 'Elizabeth', lastName: 'Hill', username: 'ehill'], [firstName: 'Alexander', lastName: 'Scott', username: 'ascott'],
                [firstName: 'Victoria', lastName: 'Green', username: 'vgreen'], [firstName: 'Benjamin', lastName: 'Adams', username: 'badams'],
                [firstName: 'Chloe', lastName: 'Baker', username: 'cbaker'], [firstName: 'William', lastName: 'Nelson', username: 'wnelson'],
                [firstName: 'Ella', lastName: 'Carter', username: 'ecarter'], [firstName: 'Joseph', lastName: 'Mitchell', username: 'jmitchell'],
                [firstName: 'Mia', lastName: 'Perez', username: 'mperez'], [firstName: 'Noah', lastName: 'Roberts', username: 'nroberts'],
                [firstName: 'Lily', lastName: 'Turner', username: 'lturner'], [firstName: 'Logan', lastName: 'Phillips', username: 'lphillips'],
                [firstName: 'Zoe', lastName: 'Campbell', username: 'zcampbell'], [firstName: 'Jayden', lastName: 'Parker', username: 'jparker'],
                [firstName: 'Hannah', lastName: 'Evans', username: 'hevans'], [firstName: 'Lucas', lastName: 'Edwards', username: 'ledwards'],
                [firstName: 'Avery', lastName: 'Collins', username: 'acollins'], [firstName: 'Mason', lastName: 'Stewart', username: 'mstewart'],
                [firstName: 'Scarlett', lastName: 'Sanchez', username: 'ssanchez'], [firstName: 'Jack', lastName: 'Morris', username: 'jmorris'],
                [firstName: 'Aria', lastName: 'Rogers', username: 'arogers'], [firstName: 'Henry', lastName: 'Reed', username: 'hreed'],
                [firstName: 'Amelia', lastName: 'Cook', username: 'acook'], [firstName: 'Sebastian', lastName: 'Morgan', username: 'smorgan'],
                [firstName: 'Layla', lastName: 'Bell', username: 'lbell'], [firstName: 'Owen', lastName: 'Murphy', username: 'omurphy'],
                [firstName: 'Ella', lastName: 'Bailey', username: 'ebailey'], [firstName: 'Gabriel', lastName: 'Rivera', username: 'grivera'],
                [firstName: 'Aubrey', lastName: 'Cooper', username: 'acooper'], [firstName: 'Carter', lastName: 'Richardson', username: 'crichardson'],
                [firstName: 'Sofia', lastName: 'Cox', username: 'scox'], [firstName: 'Wyatt', lastName: 'Howard', username: 'whoward'],
                [firstName: 'Riley', lastName: 'Ward', username: 'rward'], [firstName: 'Dylan', lastName: 'Torres', username: 'dtorres'],
                [firstName: 'Camila', lastName: 'Peterson', username: 'cpeterson'], [firstName: 'Nathan', lastName: 'Gray', username: 'ngray'],
                [firstName: 'Leah', lastName: 'Ramirez', username: 'lramirez'], [firstName: 'Elijah', lastName: 'James', username: 'ejames'],
                [firstName: 'Hazel', lastName: 'Watson', username: 'hwatson'], [firstName: 'Isaac', lastName: 'Brooks', username: 'ibrooks'],
                [firstName: 'Lillian', lastName: 'Sanders', username: 'lsanders'], [firstName: 'Matthew', lastName: 'Price', username: 'mprice']
        ]

        // === 3. Create Admin Users ===
        adminUsers.each { data ->
            User user = User.findByUsername(data.username as String)
            if (!user) {
                user = new User(
                        username: data.username,
                        password: '{noop}123',
                        businessUnit: 'IT',
                        firstName: data.firstName,
                        lastName: data.lastName
                )
                user.save(flush:true)
                println "Created admin user: ${user.username} with errors:=${user.errors}"
            } else {
                println "Admin user: ${user.username} already created"
            }

            if (!UserRole.findByUserAndRole(user, adminRole)) {
                UserRole userRole = new UserRole(user: user, role: adminRole)
                userRole.save(flush:true)
                println "Linked ${user.username} to ROLE_ADMIN"
            }
        }

        // === 4. Create Borrower Users ===
        borrowerUsers.each { data ->
            User user = User.findByUsername(data.username as String)
            if (!user) {
                user = new User(
                        username: data.username,
                        password: '{noop}123',
                        businessUnit: 'IT',
                        firstName: data.firstName,
                        lastName: data.lastName
                )
                user.save(flush:true)
                println "Created borrower user: ${user.username} with errors:=${user.errors}"
            } else {
                println "Borrower user: ${user.username} already created"
            }

            if (!UserRole.findByUserAndRole(user, borrowerRole)) {
                UserRole userRole = new UserRole(user: user, role: borrowerRole)
                userRole.save(flush:true)
                println "Linked ${user.username} to ROLE_BORROWER"
            }
        }

        createDataAuthorsBooks()
    }

    /**
     * === createDataAuthorsBooks
     *
     * Creates realistic sample authors and books if they do not already exist.
     * Associates each book with an author.
     *
     * Calls:
     * - createDataBookInstances()
     *
     * @Transactional ensures database consistency.
     */
    @Transactional
    def createDataAuthorsBooks() {

        // === 1. Generate realistic authors ===
        List<Map> authorData = [
                [firstName: 'Emily', lastName: 'Stone', dob: '1975-04-12'],
                [firstName: 'Michael', lastName: 'King', dob: '1968-11-03'],
                [firstName: 'Olivia', lastName: 'Reed', dob: '1982-06-25'],
                [firstName: 'Daniel', lastName: 'Bennett', dob: '1959-09-17'],
                [firstName: 'Sophia', lastName: 'Ward', dob: '1970-12-29'],
                [firstName: 'James', lastName: 'Brooks', dob: '1965-03-05'],
                [firstName: 'Isabella', lastName: 'Gray', dob: '1987-10-14'],
                [firstName: 'Matthew', lastName: 'Kelly', dob: '1978-07-21'],
                [firstName: 'Mia', lastName: 'Cook', dob: '1990-02-09'],
                [firstName: 'Alexander', lastName: 'Murphy', dob: '1963-01-31'],
                [firstName: 'Charlotte', lastName: 'Bailey', dob: '1984-05-16'],
                [firstName: 'David', lastName: 'Cooper', dob: '1955-08-27'],
                [firstName: 'Amelia', lastName: 'Rivera', dob: '1981-04-03'],
                [firstName: 'Benjamin', lastName: 'Richardson', dob: '1973-09-12'],
                [firstName: 'Evelyn', lastName: 'Cox', dob: '1969-06-18'],
                [firstName: 'Lucas', lastName: 'Howard', dob: '1976-02-22'],
                [firstName: 'Harper', lastName: 'Ward', dob: '1985-10-30'],
                [firstName: 'Henry', lastName: 'Torres', dob: '1960-07-04'],
                [firstName: 'Ella', lastName: 'Peterson', dob: '1989-03-27'],
                [firstName: 'Jack', lastName: 'Gray', dob: '1974-12-08'],
        ]


        authorData.each { data ->
            MyLibraryAuthor author = MyLibraryAuthor.findByFirstNameAndLastName(data.firstName as String, data.lastName as String)
            if (!author) {
                author = new MyLibraryAuthor(
                        firstName: data.firstName,
                        lastName: data.lastName,
                        dateOfBirth: new SimpleDateFormat('yyyy-MM-dd').parse(data.dob as String),
                        listOfBooks: []
                )
                author.save(flush: true)
                println "Created author: ${author.firstName} ${author.lastName} with errors:=${author.errors}"
            } else {
                println "Author: ${author.firstName} ${author.lastName} already created"
            }

        }

        // === 2. Generate books ===
        List<Map> booksData = [
                [title:"The Silent Patient", description:"A thrilling psychological mystery novel.", numberOfPages: "336"],
                [title:"Educated: A Memoir", description:"An inspiring journey through education and resilience.", numberOfPages: "352"],
                [title:"Becoming", description:"A powerful memoir exploring identity and purpose.", numberOfPages: "448"],
                [title:"Where the Crawdads Sing", description:"A captivating blend of mystery and nature writing.", numberOfPages: "384"],
                [title:"Normal People", description:"A complex love story set in Ireland.", numberOfPages: "288"],
                [title:"Atomic Habits", description:"A comprehensive guide to building good habits and breaking bad ones.", numberOfPages: "320"],
                [title:"The Subtle Art of Not Giving a F*ck", description:"A brutally honest self-help book.", numberOfPages: "224"],
                [title:"Thinking, Fast and Slow", description:"An in-depth analysis of human decision-making and psychology.", numberOfPages: "512"],
                [title:"Sapiens: A Brief History of Humankind", description:"A sweeping exploration of human history.", numberOfPages: "464"],
                [title:"Dune", description:"A classic science fiction epic.", numberOfPages: "896"],
                [title:"The Great Gatsby", description:"A timeless novel of wealth, love, and loss.", numberOfPages: "180"],
                [title:"1984", description:"A chilling dystopian classic about surveillance and control.", numberOfPages: "328"],
                [title:"To Kill a Mockingbird", description:"A powerful novel about justice and morality.", numberOfPages: "336"],
                [title:"The Catcher in the Rye", description:"A coming-of-age story of teenage alienation.", numberOfPages: "277"],
                [title:"Brave New World", description:"A dystopian vision of a technologically controlled society.", numberOfPages: "311"],
                [title:"The Alchemist", description:"A philosophical novel about destiny and dreams.", numberOfPages: "208"],
                [title:"The Road", description:"A bleak and beautiful post-apocalyptic novel.", numberOfPages: "287"],
                [title:"Gone Girl", description:"A dark psychological thriller with shocking twists.", numberOfPages: "422"],
                [title:"The Girl on the Train", description:"A gripping psychological thriller about memory and murder.", numberOfPages: "395"],
                [title:"The Book Thief", description:"A story of hope and loss in Nazi Germany.", numberOfPages: "552"],
                [title:"Life of Pi", description:"An imaginative survival story with spiritual undertones.", numberOfPages: "460"],
                [title:"The Kite Runner", description:"A novel of friendship and redemption set in Afghanistan.", numberOfPages: "371"],
                [title:"The Fault in Our Stars", description:"A touching love story between teenagers with cancer.", numberOfPages: "313"],
                [title:"Memoirs of a Geisha", description:"An immersive journey into Japanese culture and history.", numberOfPages: "434"],
                [title:"The Help", description:"A novel about race, class, and friendship in the American South.", numberOfPages: "464"],
                [title:"The Hunger Games", description:"A dystopian young adult adventure of survival.", numberOfPages: "374"],
                [title:"Catch-22", description:"A satirical novel about the absurdities of war.", numberOfPages: "453"],
                [title:"Pride and Prejudice", description:"A classic romantic novel with wit and insight.", numberOfPages: "279"],
                [title:"Jane Eyre", description:"A gothic novel exploring love and independence.", numberOfPages: "500"],
                [title:"Wuthering Heights", description:"A dark, passionate story of love and revenge.", numberOfPages: "416"],
                [title:"Little Women", description:"A timeless novel about family and womanhood.", numberOfPages: "759"],
                [title:"Beloved", description:"A haunting novel about slavery and memory.", numberOfPages: "324"],
                [title:"One Hundred Years of Solitude", description:"A magical realism epic of a family’s generations.", numberOfPages: "417"],
                [title:"The Handmaid’s Tale", description:"A dystopian novel exploring gender and power.", numberOfPages: "311"],
                [title:"The Shining", description:"A terrifying psychological horror novel.", numberOfPages: "447"],
                [title:"Dracula", description:"The classic gothic horror story of the vampire count.", numberOfPages: "418"],
                [title:"Frankenstein", description:"The original science fiction horror novel.", numberOfPages: "280"],
                [title:"Moby Dick", description:"An epic tale of obsession and revenge at sea.", numberOfPages: "635"],
                [title:"War and Peace", description:"A sweeping historical epic set during Napoleon’s invasion of Russia.", numberOfPages: "1225"],
                [title:"Crime and Punishment", description:"A psychological exploration of guilt and redemption.", numberOfPages: "671"],
                [title:"The Brothers Karamazov", description:"A philosophical novel about faith, doubt, and family.", numberOfPages: "796"],
                [title:"Les Misérables", description:"A story of injustice, redemption, and love in 19th-century France.", numberOfPages: "1463"],
                [title:"Anna Karenina", description:"A tragic novel of love and societal constraints.", numberOfPages: "864"],
                [title:"Madame Bovary", description:"A realist novel about desire and dissatisfaction.", numberOfPages: "329"],
                [title:"The Odyssey", description:"The epic Greek poem of adventure and homecoming.", numberOfPages: "541"],
                [title:"The Iliad", description:"An epic poem about heroism and the Trojan War.", numberOfPages: "683"],
                [title:"A Tale of Two Cities", description:"A historical novel set during the French Revolution.", numberOfPages: "489"],
                [title:"Great Expectations", description:"A coming-of-age story about ambition and love.", numberOfPages: "505"],
                [title:"Oliver Twist", description:"A social novel exposing the plight of orphans in Victorian London.", numberOfPages: "554"],
                [title:"David Copperfield", description:"A semi-autobiographical novel of personal growth.", numberOfPages: "624"],
                [title:"Bleak House", description:"A satirical novel about the flaws of the legal system.", numberOfPages: "768"],
        ]

        List<MyLibraryAuthor> authors = MyLibraryAuthor.list()

        booksData.eachWithIndex { data, index ->
            MyLibraryAuthor author = authors[random.nextInt(authors.size())]

            MyLibraryBook book = MyLibraryBook.findByTitle(data.title as String)
            if (!book) {
                book = new MyLibraryBook(
                        title: data.title,
                        author: author,
                        description: data.description,
                        numberOfPages: data.numberOfPages as int,
                        listOfBookInstance: []
                )
                book.save(flush:true)
                println "Created book: ${book.title} by ${book.author.firstName} ${book.author.lastName} (${data.numberOfPages} pages) with errors:=${author.errors}"
            } else {
                println "Book: ${book.title} by ${book.author.firstName} ${book.author.lastName} (${data.numberOfPages} pages) already created"
            }
        }

        createDataBookInstances()
    }


    /**
     * === createDataBookInstances
     *
     * Creates physical book instances for each book based on pre-generated counts.
     * Each instance is marked as active and available by default.
     *
     * Calls:
     * - createDataBorrowed()
     *
     * @Transactional ensures instances are inserted consistently.
     */
    @Transactional
    def createDataBookInstances() {
        // === 1. Pre-generated fixed counts for each book (0-50 instances) ===
        List<Integer> instanceCounts = [
                50, 43, 0, 39, 50, 10, 33, 30, 50, 5, 38, 4, 12, 43, 47, 0, 19, 22, 3, 17,
                14, 4, 50, 27, 43, 1, 2, 25, 31, 0, 7, 4, 20, 7, 3, 29, 0, 44, 24, 9,
                44, 8, 0, 31, 28, 13, 15, 20, 6, 17, 26, 0, 40, 27, 13, 0, 15, 10, 32, 4,
                41, 10, 22, 0, 25, 19, 4, 3, 30, 27, 9, 18, 22, 0, 8, 12, 31, 26, 18, 0,
                13, 24, 50, 11, 3, 0, 12, 5, 4, 37, 2, 49, 12, 21, 35, 0, 50, 33, 0, 17,
                44, 10, 33, 30, 50, 5, 38, 4, 12, 43, 47, 0, 10, 22, 3, 17, 14, 4, 50, 27,
                43, 1, 10, 25, 31, 0, 7, 4, 20, 7, 3, 29, 0, 44, 24, 9, 44, 8, 0, 31, 28,
                13, 15, 19, 6, 17, 26
        ]

        // === 2. Get all books ordered consistently ===
        List<MyLibraryBook> books = MyLibraryBook.list(sort: 'id')

        // === 3. Create instances per book with  ===
        books.eachWithIndex { book, idx ->
            if (book.numberOfInstances == 0) {
                int count = instanceCounts[idx]
                (1..count).each { index ->
                    MyLibraryBookInstance instance = new MyLibraryBookInstance(
                            book: book,
                            isActive: true,
                            serialNumber: random.nextInt(100000),
                            isAvailableB: true,
                            borrowHistoryOfBook: []
                    )
                    instance.save(flush: true)
                }
                println "Created ${count} instances for book: ${book.title}"
            } else {
                println "Book instances for book: ${book.title} already created"
            }
        }

        createDataBorrowed()
    }

    /**
     * === createDataBorrowed
     *
     * Generates borrowing history entries for each book instance,
     * assigning them to random borrower users with realistic durations and dates.
     *
     * Redirects to:
     * - myLibrary.listAuthor
     *
     * @Transactional ensures all borrowed entries are saved together.
     */
    @Transactional
    def createDataBorrowed() {
        Role borrowerRole = Role.findByAuthority('ROLE_BORROWER')
        List<UserRole> userRoles = UserRole.findAllByRole(borrowerRole)
        List<User> borrowerUsers = userRoles*.user

        // === 1. Get all bookInstances ===
        List<MyLibraryBookInstance> bookInstances = MyLibraryBookInstance.list(sort: 'id')


        // === 3. Generate borrowing history per bookInstance ===
        bookInstances.each { instance ->
            if (MyLibraryBorrowed.countByBookInstance(instance) > 0) {
                println "Skipping bookInstance id=${instance.id}, already has borrow records"
                return
            }

            // Generate borrow count (0–30)
            int borrowCount = random.nextInt(31)

            // Start date between Jan 1, 2020 and today minus total borrow durations
            LocalDate currentDate = LocalDate.of(2020, 1, 1).plusDays(random.nextInt(365 * 3)) // random start within 2020-2022 for spacing

            (1..borrowCount).each { idx ->
                // Borrow duration: 1–22 days
                int duration = 1 + random.nextInt(22)

                // Approval date = request date
                Date requestDate = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                Date approvalDate = null

                // Return date
                Date returnDate = null
                ApprovalStatus status
                Boolean isAvailableB = true
                if (idx == borrowCount && borrowCount % 2 == 0) {
                    // Last borrow = PENDING
                    if (borrowCount % 3 == 0) {
                        status = ApprovalStatus.APPROVED
                        approvalDate = requestDate
                    } else {
                        status = ApprovalStatus.PENDING
                    }
                    isAvailableB = false
                } else {
                    // APPROVED
                    approvalDate = requestDate
                    currentDate = currentDate.plusDays(duration)
                    returnDate = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                    status = ApprovalStatus.APPROVED

                    // Gap before next borrow: 1–30 days
                    int gap = 1 + random.nextInt(30)
                    currentDate = currentDate.plusDays(gap)
                }

                // Random user
                User user = borrowerUsers[random.nextInt(borrowerUsers.size())]

                // Create borrowed entry
                MyLibraryBorrowed borrowed = new MyLibraryBorrowed(
                        bookInstance: instance,
                        user: user,
                        requestDate: requestDate,
                        approvalDate: approvalDate,
                        returnDate: returnDate,
                        statusOfApproval: status,
                )
                instance.isAvailableB = isAvailableB
                borrowed.save(flush:false)
                instance.save(flush:false)
            }
            println "Created ${borrowCount} borrowed entries for bookInstance id=${instance.id}"
        }
        redirect controller:'myLibrary', action:'listAuthor'
    }
}