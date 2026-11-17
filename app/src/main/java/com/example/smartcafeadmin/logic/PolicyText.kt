package com.example.smartcafeadmin.logic

// Terms and Conditions and Privacy Notice shown in the app (fictional company details)
object PolicyText {

    const val TERMS_VERSION = "1.0"

    val TERMS = """
Smart Café Admin – Terms and Conditions
Version $TERMS_VERSION · November 2026

This is a student prototype. Smart Café Ltd and all contact details are fictional. Orders, customers and payments are simulated.

1. The service
The app lets authorised café staff manage the menu, update orders, review customer feedback and view sales analytics.

2. Your account
Each staff member logs in with their own email and password. Keep your password private and never share your account. New accounts start as customer accounts; only the café manager can grant admin access.

3. Permitted use
Use the app only for café work. Customer information (names, orders, notes and feedback) may only be used to prepare orders and improve the service. It must never be shared or used for personal reasons.

4. Orders
Order statuses only move forward: Pending → Preparing → Ready → Collected. Collected orders cannot be changed.

5. Problems
Report any problem or suspected misuse to the café manager: manager@smartcafe.example (fictional).

6. Changes
If these terms change, you will be asked to accept the new version.
""".trimIndent()

    val PRIVACY = """
Smart Café Admin – Privacy Notice
November 2026

Who we are
Smart Café Ltd (fictional) is the data controller. Contact: privacy@smartcafe.example (fictional).

What we collect
• Staff accounts (required): email address, role, and the date you accepted the Terms. Your password is handled by Firebase Authentication and is never stored in this app.
• Orders (required to run the café): customer first name, items, price, notes, order time and status.
• Customer feedback (optional): star rating and comment.
• Menu photos (optional): chosen with Android's photo picker. The app never accesses your camera or whole gallery, and location data is removed from photos.

Why we use it
To let staff log in securely, prepare and track orders, and see which items sell best. Personalised offers are not used; if they are introduced, customers will have to opt in.

Where it is stored
Menu, orders and feedback are stored only on this device. Staff login details and roles are stored by Google Firebase (Authentication and Cloud Firestore, London region). Data is not shared with advertisers or AI companies.

Permissions
Notifications are optional and only used for order updates. You can turn them off in Settings and the app still works.

How long we keep it
Orders and feedback: up to 1 year. Staff accounts: while you work here, and deleted on request when you leave.

Your rights
You can ask to see, correct or delete your data, or object to how it is used, by emailing privacy@smartcafe.example. You can also complain to the Information Commissioner's Office (ico.org.uk).
""".trimIndent()
}