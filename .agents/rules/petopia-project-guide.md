---
trigger: always_on
---

Work on an android app, full explanation:
Project Name: Petopia
Authors: תמר קיקוזשוילי - 214141871 and עדן בן חמו - 213778962
Mission Statement: Our goal is to be the go-to app for the pet community to share needs, knowledge, and resources, including rescue alerts, care advice, and available equipment
Target Audience: Animal welfare volunteers, pet owners, private rescuers, and compassionate community members.
User Personas
Persona: Danielle the Volunteer
Background: An active volunteer in a rescue organization, often operating remotely.
Needs: Requires real-time updates on distress reports, and always seeks additional equipment donations.
Persona: Ruth the Dog Owner
Background: Experienced dog owner, skilled in animal care, looking to donate gently used equipment.
Needs: Wants a simple way to share professional care tips and list old equipment for donation.
Functional Requirements
Core Content Types
The application revolves around three primary types of user-generated content:
Distress Report (Rescue): Allows users to post urgent calls for help when an animal is found in distress.
Care Tips & Knowledge:  Enables experienced users to share educational posts and best practices for animal care.
Equipment Donation: Allows users to offer pet equipment (food, carriers, etc.) for donation/pickup.
General Functionality (Must-Have)
Social Interaction: All content is visible to other registered users who can enrich their pet care knowledge, reach out to pets in need or just perform a good deed and donate supplies.
User Management: Users will be able to create an account, log in, log out, and stay signed in automatically.
Personal Content Management: Registered users are able to view, edit, and delete only the posts they have created.
External Data Integration: The application will consume data from The Dog API and The Cat API. These api’s provide fun facts about dogs and cats, which is a relevant knowledge to our pet loving users.
Additional Functionality (Should-Have and possible expansion)
Interactive Content: Enable registered users to comment on and like/react to other users' posts. This goes beyond simple content visibility and drives community engagement.
-------------------------------------------
Reference figma mockup:  
https://www.figma.com/make/dTjNTNy39THvMdNbzre8Zc/Social-Media-App-Mockup?t=aw6DSNj8abT8lFin-20&fullscreen=1 
follow the exect design
-------------------------------------------
Android studio and kotlin project guideliness:
1. follow the MVVM (Model-View-ViewModel) architecture
2. follow a strict Single Activity architecturecture
3. Simple data classes annotated with @Entity (for Room DB).
Dao: Interfaces for local database access.
API/Service: Interfaces for network calls.
4. Repository:
It decides whether to fetch data from the Local DB (Room) or the Network (Firebase). Do not call the DB/API directly from the UI.
5. ViewModel:
Holds the data for the UI. It calls the Repository and exposes LiveData or StateFlow to the Fragment.
6. View (UI Layer):
Fragments: Use Fragment for screens.
Activity: Only one MainActivity that acts as a container for the Fragments (using Navigation Component).
7. No Logic in Fragments: Your Fragment should only observe data.
Bad: database.save(post) inside a button click.
Good: viewModel.savePost(post) inside a button click.
Use the Layouts Folder:
8. Match the XML naming convention (e.g., fragment_feed.xml, row_post_item.xml).
9. Navigation:
Do not use Intent to move between screens (except for Camera/External). Use Navigation.findNavController(view).navigate(...).