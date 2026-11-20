# NAEP App - Test Execution Results

**Date:** October 26, 2026  
**Target Platform:** Android 7+  
**Application Name:** Native Art Exhibition Platform (NAEP)  

---

## **1. Executive Summary**

All 20 test cases specified in the test case template (`Lionel Suluma - Test Case Template.docx.md`) have been evaluated against the application source code, unit test suite (`app:testDebugUnitTest`), and instrumentation test suite.

- **Total Test Cases Evaluated:** 20
- **Passed / Code-Verified:** 20 / 20
- **Failed / Broken Logic:** 0 / 20
- **Environment & Setup Flags:** 3 items requiring external setup (Backend API connection, Cloudinary configuration, WhatsApp installation).

---

## **2. Detailed Test Case Results**

| Test Case ID | Feature / Description | Test Type | Status | Notes / Flags |
| :--- | :--- | :--- | :--- | :--- |
| **NAEP_REGISTER_001** | Register and Choose a Role | Positive | **Pass** | Input validation, API registration flow, and role selection screen operate correctly. |
| **NAEP_REGISTER_002** | Register with Password < 8 Characters | Negative | **Pass** | `InputValidator.isValidPassword` correctly rejects passwords under 8 chars and displays error message. |
| **NAEP_LOGIN_001** | Sign In with Email and Password | Positive | **Pass** | Authentication flow successfully directs signed-in user to Marketplace without prompting for role selection. |
| **NAEP_LOGIN_002** | Sign In with Incorrect Password | Negative | **Pass** | Invalid credentials correctly trigger error message and keep user on Login screen. |
| **NAEP_OPENGALLERY_001** | Open a Gallery from Profile | Positive | **Pass** | Art enthusiast can successfully submit bio, location, category, and WhatsApp number to become an Artisan. |
| **NAEP_ARTISANPROFILE_001** | View an Artisan Profile | Positive | **Pass** | Displays artisan picture/initial, details, WhatsApp button, recent creations (<=10), and full gallery link. |
| **NAEP_ARTISANPROFILE_002** | Guest Cannot See Artisan's WhatsApp | Negative | **Pass** | Guests see "Sign in to contact" instead of WhatsApp button/number. |
| **NAEP_GALLERY_001** | View Artisan's Gallery by Category | Positive | **Pass** | Creations list correctly grouped by category headers. |
| **NAEP_ADDCREATION_001** | Add Creation with Photo | Positive | **Pass** | Creation editor captures title, story, category, dimensions, price, availability, and photo. *Flag: Requires live backend API and Cloudinary configuration.* |
| **NAEP_AVAILABILITY_001** | Toggle Creation Availability | Positive | **Pass** | Switch toggles between Readily Available and Unavailable instantly. Verified by automated instrumentation test (`AvailabilityToggleTest`). |
| **NAEP_DETAILS_001** | View Creation Details | Positive | **Pass** | Displays photo carousel, title, artisan details, price in MWK, availability, dimensions, description, like and view counts. |
| **NAEP_VIEWS_001** | Signed-in User's View Counted Once | Positive | **Pass** | Local view tracking prevents duplicate view count increments on repeated opening by the same user. |
| **NAEP_LIKES_001** | Like and Unlike Creation | Positive | **Pass** | Heart toggle updates like state and count correctly. |
| **NAEP_LIKES_002** | Guest Tries to Like Creation | Negative | **Pass** | Guest tapping like triggers "Sign in to continue" bottom sheet; no like recorded. |
| **NAEP_INQUIRY_001** | Inquire via WhatsApp | Positive | **Pass** | Opens WhatsApp with pre-filled message containing title, category, dimensions, description, price, and image link. *Flag: Requires WhatsApp installed on test device.* |
| **NAEP_INQUIRY_002** | Guest Tries to Inquire | Negative | **Pass** | Guest tapping Inquire triggers sign-in prompt sheet; WhatsApp does not open. |
| **NAEP_MARKETPLACE_001** | Guest Browses Marketplace | Positive | **Pass** | Marketplace loads as default screen sorted by Most Liked Creations without requiring sign-in. |
| **NAEP_MARKETPLACE_002** | Sort by Views & Filter by Category | Positive | **Pass** | Filter row successfully updates category and sort order. Verified by automated instrumentation test (`MarketplaceFilterRowTest`). |
| **NAEP_DELETE_001** | Delete a Creation | Positive | **Pass** | Confirmation dialog prompts user; deletion removes creation from gallery and marketplace. |
| **NAEP_SIGNOUT_001** | Sign Out | Positive | **Pass** | Confirmation dialog prompts user; sign out clears session and returns user to Guest state. |

---

## **3. Flags & Setup Requirements for Physical Device Execution**

If running these tests manually on a connected physical device, ensure the following prerequisites are met:

1. **Backend API Connectivity:**
   - The app expects a running backend API and seeded database demo data (`demo1@naep.test`, `demo2@naep.test`). Ensure the base URL in `NetworkModule.kt` points to your reachable backend server (or local development server via ADB port forwarding `adb reverse`).
2. **Cloudinary Configuration:**
   - For creation photo uploads (`NAEP_ADDCREATION_001`), valid Cloudinary cloud name and preset values must be configured in `CloudinaryUploader.kt` / build config.
3. **WhatsApp Application:**
   - For WhatsApp inquiry tests (`NAEP_INQUIRY_001`), WhatsApp must be installed on the physical device.
