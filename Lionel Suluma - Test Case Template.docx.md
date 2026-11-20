**TEST CASE TEMPLATE**

Mobile Applications Development Project

*4th Year – Test Design Submission*

# **1\. Instructions**

Your test cases should be based on the project documentation you provided and the UI you have already created. You are designing the tests only and are not running them yet, so there is no Actual Result field.

* Expected results (screen names, error messages, behaviour) should match your project documentation and your UI.

* Each feature should have at least one positive test case and at least one negative test case.

* Test Case IDs must be unique and follow the format MODULE\_FEATURE\_NNN (e.g. HR\_LOGIN\_001).

* Write test steps as short numbered actions, one action per step.

* **Do not fill in the Status field. Leave it blank; it will be completed during test execution.**

* The Example Test Cases section is for guidance only and should not be included in your submission.

## **Field Guide**

| Field | What to write |
| :---- | :---- |
| Test Case ID | Unique ID in the format MODULE\_FEATURE\_NNN. |
| Description | One sentence starting with “Verify that…” describing exactly what is being tested. |
| Test Type | Positive, Negative or Boundary. |
| Priority | High, Medium or Low, based on how critical the feature is. |
| Prerequisites | What must be true before the test starts. |
| Test Data | The exact values to be used. Write “None” if no data is needed. |
| Test Steps | Numbered actions the tester performs. |
| Expected Result | What the app should do, based on your project documentation and UI. |
| Post-conditions | The state of the app or data after the test. |
| Status | Leave blank. Not to be filled in by students. |

# **2\. Project Information**

| Student Name | Lionel Suluma |
| :---- | :---- |
| **Student ID** | bed-com-37-21 |
| **Application Name** | native  art exhibition platform (NAEP) |
| **Target Platform** |  Android 7+ |
| **Date** | 26/10/2026 |
| **Template Version** | v1.0 |

# **TEST CASES**

## **Test Case 1: Register and Choose a Role**

| Test Case ID | NAEP\_REGISTER\_001 |
| :---- | :---- |
| **Description** | Verify that a new user can register with valid details and then choose the Art Enthusiast role. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. The phone number and email address below are not yet registered. |
| **Test Data** | Full name: Test User One; Phone number: 0998000111; Email address: testuser1@example.com; Password: Naep2026\!; Role: Art Enthusiast |
| **Test Steps** | 1\.       Launch the application. 2\.       Tap the “Profile” tab. 3\.       Tap “Register”. 4\.       Enter the full name in the “Full name” field. 5\.       Enter the phone number in the “Phone number” field. 6\.       Enter the email in the “Email address” field. 7\.       Enter the password in the “Password” field. 8\.       Tap the “Register” button. 9\.       On the screen “How would you like to use NAEP?”, select “Art Enthusiast”. 10\.   Tap “Yes”. |
| **Expected Result** | The account is created and the role-selection screen is displayed; after choosing Art Enthusiast/Artisan and tapping “Yes”,the open gallery screen is displayed(if the user chooses artisan role)/ marketplace screen(if the user chooses art enthusiast role)  and the Profile screen shows the role “Art Enthusiast”. |
| **Post-conditions** | A new Art Enthusiast/Artisan account exists and the user is signed in. |
| **Status** |   |

 

## **Test Case 2: Register with a Password Shorter than 8 Characters**

| Test Case ID | NAEP\_REGISTER\_002 |
| :---- | :---- |
| **Description** | Verify that registration is blocked when the password has fewer than 8 characters. |
| **Test Type** | Negative |
| **Priority** | High |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. |
| **Test Data** | Full name: Test User Two; Phone number: 0998000222; Email address: testuser2@example.com; Password: Naep1 |
| **Test Steps** | 1\.       Launch the application. 2\.       Tap the “Profile” tab. 3\.       Tap “Register”. 4\.       Enter the full name, phone number and email address. 5\.       Enter the short password in the “Password” field. 6\.       Observe the “Password” field and the “Register” button. |
| **Expected Result** | The message “At least 8 characters” is shown below the Password field; the “Register” button stays disabled; the user remains on the Register screen. |
| **Post-conditions** | No account is created. |
| **Status** |   |

 

## **Test Case 3: Sign In with Email and Password**

| Test Case ID | NAEP\_LOGIN\_001 |
| :---- | :---- |
| **Description** | Verify that a registered user can sign in and goes straight to the Marketplace without being asked to choose a role again. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. The account demo1@naep.test exists and has already chosen a role. |
| **Test Data** | Phone number or email: demo1@naep.test; Password: Password123\! |
| **Test Steps** | 1\.       Launch the application. 2\.       Tap the “Profile” tab. 3\.       Tap “Sign in”. 4\.       Enter the email in the “Phone number or email” field. 5\.       Enter the password in the “Password” field. 6\.       Tap the “Sign in” button. 7\.       Tap the “Profile” tab. |
| **Expected Result** | The Marketplace is displayed and the role-selection screen is not shown; the Profile screen shows the name “Demo Enthusiast” and the role “Art Enthusiast”. |
| **Post-conditions** | A user session is created and the user remains signed in. |
| **Status** |   |

 

## **Test Case 4: Sign In with an Incorrect Password**

| Test Case ID | NAEP\_LOGIN\_002 |
| :---- | :---- |
| **Description** | Verify that sign-in fails when a registered email address is entered with an incorrect password. |
| **Test Type** | Negative |
| **Priority** | High |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. The account demo1@naep.test exists. |
| **Test Data** | Phone number or email: demo1@naep.test; Password: WrongPass99 |
| **Test Steps** | 1\.       Launch the application. 2\.       Tap the “Profile” tab. 3\.       Tap “Sign in”. 4\.       Enter the registered email in the “Phone number or email” field. 5\.       Enter the incorrect password in the “Password” field. 6\.       Tap the “Sign in” button. |
| **Expected Result** | An error message stating that the credentials are invalid is displayed; the user remains on the Login screen. |
| **Post-conditions** | No session is created. |
| **Status** |   |

 

## **Test Case 5: Open a Gallery from the Profile**

| Test Case ID | NAEP\_OPENGALLERY\_001 |
| :---- | :---- |
| **Description** | Verify that an Art Enthusiast can open a gallery by providing a bio, location, primary category and WhatsApp number. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. The user is signed in as an Art Enthusiast who has not yet opened a gallery (for example the account from NAEP\_REGISTER\_001). |
| **Test Data** | Bio: I carve Malawian wildlife in mahogany.; Location: Zomba; Primary art category: Carvings (Wood works); WhatsApp number: the pre-filled phone number |
| **Test Steps** | 1\.       Tap the “Profile” tab. 2\.       Tap “Open a gallery”. 3\.       Enter the bio in the “Bio (your creative journey)” field. 4\.       Enter the location in the “Location” field. 5\.       Open the “Primary art category” drop-down and select “Carvings (Wood works)”. 6\.       Leave the “WhatsApp number” field as pre-filled. 7\.       Tap the “Open a gallery” button. |
| **Expected Result** | The “My Gallery” screen is displayed; the “My Gallery” tab appears in the bottom bar; the Profile screen shows the role “Artisan”. |
| **Post-conditions** | An artisan profile exists for the user and artisan features are available. |
| **Status** |   |

 

## **Test Case 6: View an Artisan Profile**

| Test Case ID | NAEP\_ARTISANPROFILE\_001 |
| :---- | :---- |
| **Description** | Verify that the artisan profile shows the artisan’s details, a WhatsApp button, recent creations and a link to the full gallery. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. The user is signed in as demo1@naep.test; at least one artisan has creations. |
| **Test Data** | Any creation listed in the Marketplace |
| **Test Steps** | 1\.       Tap the “Marketplace” tab. 2\.       Tap any creation card. 3\.       Tap the artisan’s name or avatar on the details screen. 4\.       Scroll through the “Artisan Profile” screen. |
| **Expected Result** | The “Artisan Profile” screen shows the artisan’s picture or initial, name, location, bio and category tag; a “Contact on WhatsApp” button; the “Recent creations” section with at most 10 creations; and a “View full gallery” button. |
| **Post-conditions** | No data is changed. |
| **Status** |   |

 

## **Test Case 7: Guest Cannot See the Artisan’s WhatsApp Contact**

| Test Case ID | NAEP\_ARTISANPROFILE\_002 |
| :---- | :---- |
| **Description** | Verify that a Guest viewing an artisan profile is asked to sign in instead of seeing the WhatsApp contact. |
| **Test Type** | Negative |
| **Priority** | High |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. The user is not signed in (Guest). |
| **Test Data** | Any creation listed in the Marketplace |
| **Test Steps** | 1\.       Launch the application. 2\.       Tap any creation card in the Marketplace. 3\.       Tap the artisan’s name or avatar. 4\.       Observe the contact button. |
| **Expected Result** | The button reads “Sign in to contact” instead of “Contact on WhatsApp”, and no phone or WhatsApp number is visible on the screen. |
| **Post-conditions** | The user remains a Guest and no data is changed. |
| **Status** |   |

 

## **Test Case 8: View an Artisan’s Gallery Grouped by Category**

| Test Case ID | NAEP\_GALLERY\_001 |
| :---- | :---- |
| **Description** | Verify that the gallery lists all of an artisan’s creations grouped by category. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. An artisan exists with creations in at least two different categories. |
| **Test Data** | Artisan with creations in two or more categories (for example Paintings and Pottery and Ceramics) |
| **Test Steps** | 1\.       Launch the application. 2\.       Tap a creation card and then the artisan’s name. 3\.       On the “Artisan Profile” screen, tap “View full gallery”. 4\.       Scroll through the “Gallery” screen. |
| **Expected Result** | All creations of the artisan are listed, grouped under one heading per category, each shown as a creation card. |
| **Post-conditions** | No data is changed. |
| **Status** |   |

 

## **Test Case 9: Add a Creation with a Photo**

| Test Case ID | NAEP\_ADDCREATION\_001 |
| :---- | :---- |
| **Description** | Verify that an artisan can add a creation with a title, story, category, dimensions, price, availability and a photo. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. The user is signed in as demo2@naep.test (Artisan); photo uploads (Cloudinary) are configured; the phone has at least one photo. |
| **Test Data** | Title: Fisherman at Sunset; Description: Acrylic on canvas of a fisherman on Lake Malawi.; Category: Paintings; Dimensions: 60 cm x 90 cm; Price (MWK): 85000; Availability: Readily Available; Photo: one image from the phone |
| **Test Steps** | 1\.       Tap the “My Gallery” tab. 2\.       Tap “Add Creation”. 3\.       Tap “Add photos” and select one photo. 4\.       Enter the title in the “Title” field. 5\.       Enter the story in the “Description / Story behind the piece” field. 6\.       Select “Paintings” in the “Category” drop-down. 7\.       Enter the dimensions in the “Dimensions” field. 8\.       Enter the price in the “Price (MWK)” field. 9\.       Confirm the availability status shows “Readily Available”. 10\.   Tap “Save”. |
| **Expected Result** | The user returns to My Gallery; the creation “Fisherman at Sunset” appears with the price “MWK 85,000” and the badge “Readily Available”; the creation also appears in the Marketplace. |
| **Post-conditions** | The creation and its photo are stored; the photo is hosted on Cloudinary. |
| **Status** |   |

 

## **Test Case 10: Toggle a Creation’s Availability**

| Test Case ID | NAEP\_AVAILABILITY\_001 |
| :---- | :---- |
| **Description** | Verify that an artisan can switch a creation between Readily Available and Unavailable with one tap. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. The user is signed in as an Artisan who owns a creation currently marked “Readily Available”. |
| **Test Data** | Any of the artisan’s creations marked Readily Available |
| **Test Steps** | 1\.       Tap the “My Gallery” tab. 2\.       Find a creation showing “Readily Available”. 3\.       Tap the switch next to the badge once. 4\.       Tap the “Marketplace” tab and find the same creation. |
| **Expected Result** | The badge changes to “Unavailable” immediately after the single tap; the Marketplace card for the same creation also shows “Unavailable”. |
| **Post-conditions** | The availability status is stored as Unavailable; tapping the switch again restores Readily Available. |
| **Status** |   |

 

## **Test Case 11: View the Details of a Creation**

| Test Case ID | NAEP\_DETAILS\_001 |
| :---- | :---- |
| **Description** | Verify that the creation details screen shows the photos, attributes, availability, counts and a tappable artisan. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. At least one creation with a price exists. |
| **Test Data** | Any creation listed in the Marketplace |
| **Test Steps** | 1\.       Launch the application. 2\.       Tap any creation card in the Marketplace. 3\.       Swipe the photo carousel left and right. 4\.       Scroll down the details screen. 5\.       Tap the artisan’s name or avatar. |
| **Expected Result** | The screen shows a swipeable photo carousel, the title, the artisan’s name and avatar, the price in MWK (or “Price on request”), the availability badge, category, dimensions, description, the like count and the view count; tapping the artisan opens the “Artisan Profile” screen. |
| **Post-conditions** | No data is changed. |
| **Status** |   |

 

## **Test Case 12: A Signed-in User’s View Is Counted Once**

| Test Case ID | NAEP\_VIEWS\_001 |
| :---- | :---- |
| **Description** | Verify that a signed-in user’s first opening of a creation increases its view count by one and that opening it again does not increase it further. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. The user is signed in as demo1@naep.test and has never opened the creation below; the creation is not owned by this user. |
| **Test Data** | A creation the user has not opened before; note its view count (N) |
| **Test Steps** | 1\.       Tap the “Marketplace” tab. 2\.       Tap a creation card and note the view count (N). 3\.       Press back and tap the same creation card again; read the view count. 4\.       Press back and open the same creation a third time; read the view count. |
| **Expected Result** | The view count is N \+ 1 after the first opening was counted and stays N \+ 1 after the repeated openings. |
| **Post-conditions** | One view by this user is stored for the creation. |
| **Status** |   |

 

## **Test Case 13: Like and Unlike a Creation**

| Test Case ID | NAEP\_LIKES\_001 |
| :---- | :---- |
| **Description** | Verify that a signed-in user can like a creation and then remove the like, with the like count changing each time. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. The user is signed in as demo1@naep.test; the creation is not owned by this user and has not been liked by them. |
| **Test Data** | A creation not yet liked by the user; note its like count (N) |
| **Test Steps** | 1\.       Tap the “Marketplace” tab. 2\.       Tap a creation card and note the like count (N). 3\.       Tap the heart icon. 4\.       Tap the heart icon again. |
| **Expected Result** | After the first tap the heart is filled and the like count is N \+ 1; after the second tap the heart is an outline again and the like count is back to N. |
| **Post-conditions** | The like is removed; no like by this user remains. |
| **Status** |   |

 

## **Test Case 14: Guest Tries to Like a Creation**

| Test Case ID | NAEP\_LIKES\_002 |
| :---- | :---- |
| **Description** | Verify that a Guest tapping the like button is asked to sign in and no like is recorded. |
| **Test Type** | Negative |
| **Priority** | High |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. The user is not signed in (Guest). |
| **Test Data** | Any creation listed in the Marketplace; note its like count (N) |
| **Test Steps** | 1\.       Launch the application. 2\.       Tap any creation card and note the like count (N). 3\.       Tap the heart icon. 4\.       Read the sheet that appears. 5\.       Tap “Cancel”. |
| **Expected Result** | A sheet titled “Sign in to continue” appears with the message “Sign in or register to like this creation.” and the buttons “Sign in” and “Cancel”; after “Cancel” the sheet closes and the like count is still N. |
| **Post-conditions** | No like is recorded; the user remains a Guest. |
| **Status** |   |

 

## **Test Case 15: Inquire About a Creation via WhatsApp**

| Test Case ID | NAEP\_INQUIRY\_001 |
| :---- | :---- |
| **Description** | Verify that a signed-in user can tap Inquire Creation and WhatsApp opens a chat with the artisan with a pre-filled message. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. The user is signed in as demo1@naep.test; WhatsApp is installed on the phone; the creation is not owned by this user and has a price. |
| **Test Data** | Any creation with a price listed in the Marketplace |
| **Test Steps** | 1\.       Tap the “Marketplace” tab. 2\.       Tap a creation card. 3\.       Tap the “Inquire Creation” button. 4\.       Read the message in the WhatsApp message box without sending it. |
| **Expected Result** | WhatsApp opens a chat with the artisan’s number; the message box is pre-filled with the creation’s title, category, dimensions, description, price in MWK, availability and an image link. |
| **Post-conditions** | The message is not sent until the user taps send; the creation data is unchanged. |
| **Status** |   |

 

## **Test Case 16: Guest Tries to Inquire About a Creation**

| Test Case ID | NAEP\_INQUIRY\_002 |
| :---- | :---- |
| **Description** | Verify that a Guest tapping Inquire Creation is asked to sign in and WhatsApp is not opened. |
| **Test Type** | Negative |
| **Priority** | High |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. The user is not signed in (Guest). |
| **Test Data** | Any creation listed in the Marketplace |
| **Test Steps** | 1\.       Launch the application. 2\.       Tap any creation card. 3\.       Tap the “Inquire Creation” button. 4\.       Read the sheet that appears. |
| **Expected Result** | A sheet titled “Sign in to continue” appears with the message “Sign in or register to inquire about this creation.”; WhatsApp is not opened. |
| **Post-conditions** | The user remains a Guest and no inquiry is recorded. |
| **Status** |   |

 

## **Test Case 17: Guest Browses the Marketplace**

| Test Case ID | NAEP\_MARKETPLACE\_001 |
| :---- | :---- |
| **Description** | Verify that a Guest can open the app and browse creations without signing in, sorted by Most Liked Creations by default. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. The user is not signed in (Guest). |
| **Test Data** | None |
| **Test Steps** | 1\.       Launch the application. 2\.       Observe the first screen. 3\.       Scroll through the creation cards. |
| **Expected Result** | The Marketplace is the first screen; the chips “Most Liked Creations” and “All” are selected; the cards are ordered with the highest like count first; each card shows a photo, title, artisan name, price or “Price on request”, availability badge and like and view counts; no sign-in is required. |
| **Post-conditions** | No data is changed. |
| **Status** |   |

 

## **Test Case 18: Sort by Most Viewed and Filter by Category**

| Test Case ID | NAEP\_MARKETPLACE\_002 |
| :---- | :---- |
| **Description** | Verify that selecting Most Viewed Creations and a category chip shows only that category’s creations ordered by view count. |
| **Test Type** | Positive |
| **Priority** | High |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. At least two creations exist in the category used. |
| **Test Data** | Sort chip: Most Viewed Creations; Category chip: Pottery and Ceramics |
| **Test Steps** | 1\.       Launch the application. 2\.       Tap the “Most Viewed Creations” chip. 3\.       Tap the “Pottery and Ceramics” chip. 4\.       Scroll through the creation cards. |
| **Expected Result** | The “Most Viewed Creations” and “Pottery and Ceramics” chips are selected; only creations in that category are listed, ordered with the highest view count first. |
| **Post-conditions** | No data is changed. |
| **Status** |   |

 

## **Test Case 19: Delete a Creation**

| Test Case ID | NAEP\_DELETE\_001 |
| :---- | :---- |
| **Description** | Verify that an artisan can delete one of their creations after confirming the Yes/Cancel dialog. |
| **Test Type** | Positive |
| **Priority** | Medium |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. The user is signed in as an Artisan who owns a creation that can be deleted (for example the creation added in NAEP\_ADDCREATION\_001). |
| **Test Data** | The creation Fisherman at Sunset (or any test creation) |
| **Test Steps** | 1\.       Tap the “My Gallery” tab. 2\.       Tap the delete icon on the creation. 3\.       Read the dialog “Delete this creation?”. 4\.       Tap “Yes”. 5\.       Tap the “Marketplace” tab. |
| **Expected Result** | The creation is removed from My Gallery and no longer appears in the Marketplace. |
| **Post-conditions** | The creation is removed from public listings. |
| **Status** |   |

 

## **Test Case 20: Sign Out**

| Test Case ID | NAEP\_SIGNOUT\_001 |
| :---- | :---- |
| **Description** | Verify that a signed-in user can sign out after confirming the Yes/Cancel dialog. |
| **Test Type** | Positive |
| **Priority** | Medium |
| **Prerequisites** | App installed on an Android phone; backend API running and reachable; database seeded with the demo data. The user is signed in as demo1@naep.test. |
| **Test Data** | None |
| **Test Steps** | 1\.       Tap the “Profile” tab. 2\.       Tap “Sign out”. 3\.       Read the dialog “Do you want to sign out?”. 4\.       Tap “Yes”. |
| **Expected Result** | The user is returned to the Marketplace as a Guest; the Profile screen shows “You are browsing as a Guest” with the “Sign in” and “Register” buttons; the “My Gallery” tab is not shown. |
| **Post-conditions** | The session is ended and the stored login tokens are cleared. |
| **Status** |   |

 

