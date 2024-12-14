# Running Tracker App

The *Running Tracker App* is a modern Android application that helps users track their runs. It records and displays key statistics such as average speed, calories burned, the total run time, and the date of the run. The app also visualizes all recorded runs in an interactive line chart, allowing users to analyze their progress over time.

> *This app was built as part of Philipp Lackner's comprehensive Android development course.* It showcases key concepts and tools for building robust Android applications.

## Features

- *Run Tracking:* Tracks your running session using location providers, integrating with Google Maps API.
- *Statistics Display:* Calculates and displays:
  - Average speed
  - Calories burned
  - Total running time
  - Date of the run
- *Run History:* Displays all runs in a dynamic *line chart* for easy visualization.
  ![2](https://github.com/user-attachments/assets/2011b64b-d033-4113-8f1e-7f7ae9860d5f)
![1](https://github.com/user-attachments/assets/8ff2199c-cfc2-4990-b2da-29b5394946ca)
- *Sorting Options:* Sort runs by date, distance, duration, or calories burned using a *spinner*.
- ![4](https://github.com/user-attachments/assets/20661407-d209-4076-b0ce-e0e12118346a)
- *Interactive Map:* View your running route on a Google Map during and after your run.
 ![WhatsApp Image 2024-12-14 at 22 22 03_f60d56d7](https://github.com/user-attachments/assets/59009b8a-f612-44c8-9859-d02d6fa1ce6a)
- *Notifications:* Ongoing notification to display run stats while tracking.
  ![WhatsApp Image 2024-12-14 at 22 22 03_6230204f](https://github.com/user-attachments/assets/a83b5a91-d7f3-40ab-8673-e410f4948e45)
- *Floating Action Button (FAB):* Provides quick access to start, pause, or reset a run.
- *Navigation:* User-friendly navigation using a *Navigation Graph* and buttons for seamless transitions between screens.
- ![5](https://github.com/user-attachments/assets/fafb079d-4230-4bf0-b80b-8763fff6a687)
![6](https://github.com/user-attachments/assets/9c87a477-0524-4b6f-bbe0-4c30ba0eda74)
![7](https://github.com/user-attachments/assets/7d358f08-c69e-420e-97d1-8f8d1b7b5698)

- *LineChart:* analyzing users runs and visualizes all recorded runs in an interactive.
![3](https://github.com/user-attachments/assets/a9eb233b-e292-4c8e-a525-6e6153d5e3fa)


## Technical Details

The application utilizes modern Android development practices and libraries, ensuring maintainability, scalability, and performance.

### Key Technologies and Features

1. *MVVM Architecture*  
   A clean and organized architecture separating UI, data, and business logic.

2. *Room Database*  
   Efficient local data storage to persist user runs.

3. *Dagger Hilt*  
   Dependency injection framework for better code reusability and testing.

4. *Google Map API*  
   Provides map functionalities to track and display the running route.

5. *Location Request*  
   Accesses GPS and location services to record user routes and calculate distance.

6. *Fragments*  
   Modular UI components for better reusability and navigation.

7. *Navigation Graph*  
   Simplifies navigation between fragments and manages the back stack effectively.

8. *Notifications*  
   Keeps users informed about their run stats during an active session.

9. *Floating Action Button (FAB)*  
   Quick access to primary actions like starting and stopping runs.

10. *Line Chart Integration*  
    Visualizes user progress using a line chart for a better analytical experience.

11. *Spinner*  
    Allows sorting runs by different criteria for personalized data views.
