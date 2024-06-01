# projects.github.io
Welcome to my personal project portfolio! Here you will find a range of all the relevant programming projects I have done over the course of my uni degree (at UNSW). This is not to say that these were the only projects I did. Rather, these were all the most relevant, high-scoring projects. 

I have attempted in the past to create my own portfolio website from scratch. However, it has not been updated recently. It remains undeveloped, due to the fact that I want to recreate it using industry grade tools (such as React, Bootstrap and AWS deployment), but feel free to see my first attempt at frontend programming!

https://aidantze.github.io/

I will list all projects, and provide a brief description for each, below (sorted by most recent project or uni course):

Last updated: 1/6/24

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

<img width="285" alt="Screen Shot 2024-03-15 at 12 52 46" src="https://github.com/aidantze/projects.github.io/assets/122945487/8b12ee28-64b5-45f2-a36e-9d847cbd1b9b">

*img: model-based agent successfully playing Hashiwokakero*


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

This course was an entire workshop focusing on requirements and design, where our group built an e-invoicing web service and an API deployed using AWS and swagger to be used by other groups. 

### se2021-23t1-einvoicing-api-h10a-brownie-storage-api

*Group Project*

API which uses PostgreSQL to store relevant e-invoicing data in a storage API. This API was managed using GitHub, documented on Swagger and deployed using AWS Elastic Beanstalk. The storage API was the most popular in terms of usage by other groups in the course, compared to other storage APIs. 

### se2021-23t1-einvoicing-frontend-h10a-brownie-storage-api

*Group Project*

Web service for users to create, manage and send invoices to other users easily and securely. The focus of the web service was on accessibility for visually impaired users (inspired by a true story of a blind accountant). The web services was also managed, documented and deployed using the same industry-grade services as the storage API. My main contribution was in design and development of the frontend, as well as pitching our services to a live panel of stakeholders.

<img width="1364" alt="Screen_Shot_2023-04-19_at_20 09 42" src="https://github.com/aidantze/projects.github.io/assets/122945487/79c2c9e1-c1be-4a2e-ab70-e583dde860b7">

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

One of the assignments involved applying the Dutch Flag Sort algorithm to efficiently sorting a sample of DNA, of which there would be billions and billions of DNA-pairs, and 4 different permuations of these pairs to sort through. In terms of Big-O notation for time complexity, the Dutch Flag Sort algorithm reduces an O(n^2) sorting algorithm to an O(n) sorting algorithm. 

<img width="595" alt="Screen Shot 2024-06-01 at 21 14 14" src="https://github.com/aidantze/projects.github.io/assets/122945487/c44486d9-0ff9-460d-bd4c-8bc3f4971cf0">

*img: code screenshot of formal verification for DNA sorting algorithm*


## comp2521 - Data Structures and Algorithms

### ass2 - Pagerank
An assignment which uses graph algorithms such as Dijkstra path find, and advanced mathematical formulas, to calculate the pagerank value for web pages, to assist in recommendation of pages to a user in a search engine. 

### lab07 - Maze Simulation
A lab exercise which tests the effectiveness of Depth First Search and Breadth First Search in solving a maze.

### ass1 - Flight Database
An assignment which uses a tree data structure to perform operations in managing a basic flight database.


