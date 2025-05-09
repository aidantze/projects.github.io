# projects.github.io
Welcome to my personal project portfolio! Here you will find a range of all the relevant programming projects I have done over the course of my uni degree (at UNSW). This is not to say that these were the only projects I did. Rather, these were all the most relevant, high-scoring projects. 

I have attempted in the past to create my own portfolio website from scratch. However, it has not been updated recently. It remains undeveloped, due to the fact that I want to recreate it using industry grade tools (such as React, Bootstrap and AWS deployment), but feel free to see my first attempt at frontend programming!

https://aidantze.github.io/

I will list all projects, and provide a brief description for each, below (sorted by most recent project or uni course):

Last updated: 1/5/25

## seng3011 - Software DevOps Workshop

*Industry-relevant project*

This course was an entire workshop focusing on DevOps practices, such as continuous integration, delivery and deployment, containerisation, testing practices, and site reliability monitoring/observability. 

I led a 5-member software team where the group built **_KeyTrend_**, a news keyword microservice designed to enhance news article fetching from multiple data sources, including NewsAPI and the Australian Financial Review, and use AI insights to extract relevant keywords, summarise articles and visualise keyword usage in articles over time. We had to pitch the project to a live panel of stakeholders, and demonstrate the behaviour of the deployed service.

My role in the project was very substantial. I configured all of the github repo's ci/cd pipeline, deployed to Render, build the service's Swagger documentation and managed all Jira tasks, plus a few backend routes and frontend service layer integrations.

**Links**
- Deployed frontend: https://keytrend.onrender.com
- Deployed backend: https://keytrend-api.onrender.com
- Swagger OpenAPI documentation: https://app.swaggerhub.com/apis-docs/aidan-bbf/KeyTrend/1.0.0#/

Note that the links above may not work if the services have been depreciated.

### SENG3011_H17A_OMEGA-nodeapp

*Group Project*

Microservice API built using express.js. Connects to MongoDB Atlas instance which stores news articles and fetches them in real-time (approx. once a day). This API was managed using GitHub, with a ci/cd pipeline that performs status checks and generates test and coverage reports with AI summaries to inform developers which files are not covered or which tests are failing. It was deployed using Render, with GitHub actions that automatically push code to Render, and Swagger OpenAPI documentation configured for the deployed environment. 

The microservice also uses a number of external APIs. It fetches from NewsAPI and AFR dataset API. It uses Gemini to extract keywords from articles, and natural to perform the same action if Gemini quota is reached. Plans to also use Gemini for article summarisation did not get implemented in time, due to budget issues. 

The microservice API was also used by 3 other groups in the course, making it robust and useful for integrating with external services.

<img width="932" alt="Screenshot 2025-04-04 at 13 59 06" src="https://github.com/user-attachments/assets/d5c29887-4ea1-4d76-80b1-2ce098a65f49" />

*img: screenshot of github pull request with status checks all passing*

<img width="1301" alt="Screenshot 2025-04-29 at 12 04 23" src="https://github.com/user-attachments/assets/f098e8b2-1e12-46fb-81f0-1d0e60f29a40" />

*img: screenshot of Swagger OpenAPI documentation for deployed backend*

<img width="1088" alt="Screenshot 2025-04-29 at 14 29 48" src="https://github.com/user-attachments/assets/00b3bec5-9489-470e-82de-4365f4b82929" />

*img: screenshot of Swagger healthcheck route returning "alive" status in deployed backend*

### SENG3011_H17A_OMEGA-web-app

*Group Project*

Web service for users to fetch articles, extract keywords from articles, visualise keyword usage in articles over time on a live graph, and keep up with keyword trends. This service was built using React.js, with axios library used to manage the service layer, which calls the backend. The service was managed, documented and deployed using the same industry-grade tools as the microservice API. 

<img width="1203" alt="Screenshot 2025-04-18 at 14 33 10" src="https://github.com/user-attachments/assets/e18c8fb1-d4e7-4df7-ae32-833a6542f333" />

*img: screenshot of frontend displaying extracted keywords from backend route via service layer*

<img width="1347" alt="Screenshot 2025-04-24 at 14 47 31" src="https://github.com/user-attachments/assets/2ef19b8f-55bc-4340-8047-5d44c2660a81" />

*img: screenshot of frontend displaying current trending keywords and graphs from backend route via service layer*

<img width="1272" alt="Screenshot 2025-04-29 at 15 17 45" src="https://github.com/user-attachments/assets/83509ee9-a9c5-4302-81e2-3c6a6e69730f" />

*img: screenshot of Render logs for deployed frontend*

## comp3331 - Computer Networks and Applications
This networks assignment involved the creation of **_SockForums_ 🧦**, an online forum application that operates in the terminal environment and communicates between client and server using UDP and TCP protocols.

Users register/login with a username and password stored in a credentials.txt file in the server (yes, this isn't very secure, but it had to be implemented according to assignment specification). Threads are created and stored in individual files in the server containing messages and file upload notifications. Users can create, list and delete threads, and create, read, edit and delete messages, plus they can request to upload a file to a thread or download a file from a thread. Finally, the user can logout safely. 

UDP was used for all command interactions, while TCP was reserved specifically for file transfers. Multi-threading was implemented to handle concurrency, so the server can handle two users logging in with the same username simultaneously. Error messages are communicated from server and client to prompt retransmission in the event of packet loss. 

<img width="1079" alt="Screenshot 2025-04-30 at 15 12 52" src="https://github.com/user-attachments/assets/2dbf052a-288b-4138-8a13-50dcc8731c3f" />

*img: screenshot of terminal with 1 server and 2 clients over UDP connection, clients simultaneously login with same username, only 1 is accepted*

## comp9321 - Data Services Engineering

### ass3 - Machine Learning
This machine learning assignment does 2 things:
1. Predicts the age of a policyholder in a car insurance company using a regression model. This model aims to reduce mean squared error
2. Predicts if a policyholder in a car insurance company will lodge a claim using a classification model. This model aims to maximise F1-score while reducing overfitting

<img width="655" alt="Screen Shot 2024-06-01 at 21 16 23" src="https://github.com/aidantze/projects.github.io/assets/122945487/0874eb12-3ec5-47d5-8ac2-4b7eed58bb6b">

*img: code screenshot of testing different models for regression*

### ass2 - RESTful API

*One of my favourite assignments I've ever done!*

Created a RESTful API which extracts relevant data from the Deutsche Bahn (German high speed train network) database and uses Google Gemini to create a tourism guide. The API contains the following features, all polished with endpoints and swaggerdoc adhering to RESTful API Design guidelines:
- Create and edit stops
- Get info about a stop
- Edit a stop
- Delete a stop
- Get operator profiles
- Get guide

This Data Service stores all data in an SQLite database, querying when necessary.

<img width="688" alt="Screen Shot 2024-06-01 at 21 15 28" src="https://github.com/aidantze/projects.github.io/assets/122945487/56bec5f5-c437-47fd-a7fc-c9588c4186b5">

*img: code screenshot of update stop endpoint*

<img width="1353" alt="Screen Shot 2024-04-06 at 14 21 08" src="https://github.com/aidantze/projects.github.io/assets/122945487/e854ae7f-a378-48f2-ac4d-23ea2592ce1a">

*img: swagger doc of my entire RESTful API*


### ass1 - Data Cleansing, Manipulation, Visualisation
Extracts data from a csv file, stores such info in python dataframes, cleans and manipulates data according to a set of criteria, and visualises the data according to most relevant and informative attributes. 

<img width="593" alt="Screen Shot 2024-06-01 at 21 19 13" src="https://github.com/aidantze/projects.github.io/assets/122945487/1c25ebc8-688f-489f-b81d-bb737d0a2f1d">

*img: code screenshot of one of the subplots for data visualisation*

![z5360925-Q12](https://github.com/aidantze/projects.github.io/assets/122945487/1f0549c4-d51a-4bc7-916a-39e536011940)

*img: visualisation of data summary after data cleansing and manipulation*

## comp3411 - Artificial Intelligence

### ass2 - Search Algorithms Report
Report which analyses different search algorithms, including heuristic path search evaluation and alpha-beta pruning for 2-player game simulation.

### ass1 - Constraint Satisfaction Agent
Uses constraint satisfaction to play a modified game of Hashiwokakero: placement of bridges between adjacent islands which must satisfy the number on the island. The algorithm uses forward checking and combinatorics to try all possible combinations of bridge placements at any given island before moving to the next island, and checks if all constraints are satisfied before terminating. 

Different characters correspond to a different number of bridges between island numbers, and no bridge can cross over another bridge:
**\-** 1 horizontal bridge
**=** 2 horizontal bridges
**E** 3 horizontal bridges
**|** 1 vertical bridge
**"** 2 vertical bridges
**\#** 3 vertical bridges

<img width="285" alt="Screen Shot 2024-03-15 at 12 52 46" src="https://github.com/aidantze/projects.github.io/assets/122945487/8b12ee28-64b5-45f2-a36e-9d847cbd1b9b">

*img: model-based agent successfully playing Hashiwokakero in a terminal environment*


## fins3646 - Toolkit for Finance

### project2 - Event Study

*Group Project*

A project that performed some introductory data manipulation using pandas, before using the data to analyse whether total volatility has any effect on a long-short portfolio consisting of top-performing NYSE stocks. The event study was deliberately made unclear for us to figure out the null hypothesis for ourselves. 


## comp2511 - Object-Oriented Design and Programming

### assignment-ii - Dungeon-crawler game

*Pair Project*

Improves upon the current dungeon-crawler game by implementing new features. My partner worked on a logic gates feature. Meanwhile I worked on the snakes feature, which is a new enemy with complex behaviour, and evolution of requirements, which involved creating goals for takedown of enemies. In addition I did the majority of the open refactoring section, and contributed to both individual and pair blogging. The focus of this assignment was on design patterns and refactoring in Java. 

<img width="830" alt="Screen Shot 2024-06-01 at 20 58 10" src="https://github.com/aidantze/projects.github.io/assets/122945487/81ddf480-4829-4064-8e1f-87de6f3c3522">

*img: pair blog documenting changes we made during open refactoring*

<img width="819" alt="Screen Shot 2024-06-01 at 20 58 04" src="https://github.com/aidantze/projects.github.io/assets/122945487/755d18d7-edf4-422e-bf51-a44ef8031c73">

*img: pair blog documenting changes I made while implementing the Snakes feature*

### assignment-i - Satellite Simulation
Simulates the orbit of satellites around Jupiter using basic object-oriented programming techniques in Java. The focus of this assignment was on streams and debugging in Java. 


## seng2021 - Software Requirements and Design Workshop

*Industry-relevant project*

This course was an entire workshop focusing on requirements and design in API development. 

I led a 5-member software team, where the group built **_ei-ei_**, an e-invoicing web service and storage API deployed using AWS and swagger to be used by other groups. My role in the service was in some backend routes, and most frontend component designs, as well as managing Jira tasks. 

### se2021-23t1-einvoicing-api-h10a-brownie-storage-api

*Group Project*

API which uses PostgreSQL to store relevant e-invoicing data in a storage API. This API was managed using GitHub, documented on Swagger and deployed using AWS Elastic Beanstalk. The storage API was the most popular in terms of usage by other groups in the course, compared to other storage APIs.

### se2021-23t1-einvoicing-frontend-h10a-brownie-storage-api

*Group Project*

Web service for users to create, manage and send invoices to other users easily and securely. The focus of the web service was on accessibility for visually impaired users (inspired by a true story of a blind accountant). The web services was also managed, documented and deployed using the same industry-grade services as the storage API. My main contribution was in design and development of the frontend, as well as pitching our services to a live panel of stakeholders.

![IMG_0045](https://github.com/aidantze/projects.github.io/assets/122945487/e97c0226-3035-4179-a284-82cb98e34285)

*img: prototype design of the frontend web service*

<img width="1364" alt="Screen_Shot_2023-04-19_at_20 09 42" src="https://github.com/aidantze/projects.github.io/assets/122945487/0efcb190-7ac3-464d-a8d7-d162ebc31da1">

*img: implementation of the frontend web service, with many bugs and UI issues to be fixed. At this stage, testing the backend APIs were the priority*


## comp3311 - Database Systems

### ass2 - Pokemon Database
An assignment which uses both Python and SQL to store pokemon data, and uses this data to calculate attack values in a simulated pokemon duel.

### ass1 - Beer Database
An assignment which uses SQL to store and manage data about different kinds of beer produced in/imported to Australia. The focus of this assignment was on basic database techniques including merging tables. 


## seng2011 - Formal Verification workshop

This course was a formal verification course, which used mathematical predicates and Dafny to prove the validity of all internal statements within an algorithm or function (white-box testing). 

One of the assignments involved applying the Dutch Flag Sort algorithm to efficiently sorting a sample of DNA, of which there would be billions and billions of DNA-pairs, and 4 different permuations of these pairs to sort through. In terms of Big-O notation for time complexity, the Dutch Flag Sort algorithm reduces an O(n^2) sorting algorithm to an O(n) sorting algorithm (hence why it's my favourite sorting algorithm!).

<img width="595" alt="Screen Shot 2024-06-01 at 21 14 14" src="https://github.com/aidantze/projects.github.io/assets/122945487/c44486d9-0ff9-460d-bd4c-8bc3f4971cf0">

*img: code screenshot of formal verification for DNA sorting algorithm*


## comp2521 - Data Structures and Algorithms

### ass2 - Pagerank
An assignment which uses graph algorithms such as Dijkstra path find, and advanced mathematical formulas, to calculate the pagerank value for web pages, to assist in recommendation of pages to a user in a search engine. 

### lab07 - Maze Simulation
A lab exercise which tests the effectiveness of Depth First Search and Breadth First Search in solving a maze.

### ass1 - Flight Database
An assignment which uses a tree data structure to perform operations in managing a basic flight database.


