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

### ass1 - Data Cleansing, Manipulation, Visualisation
Extracts data from a csv file, stores such info in python dataframes, cleans and manipulates data according to a set of criteria, and visualises the data according to most relevant and informative attributes. 


## comp3411 - Artificial Intelligence

### ass2 - Search Algorithms Report
Report which analyses different search algorithms, including heuristic path search evaluation and alpha-beta pruning for 2-player game simulation.

### ass1 - Constraint Satisfaction Agent
Uses constraint satisfaction to play a modified game of Hashiwokakero: placement of bridges between adjacent islands which must satisfy the number on the island. The algorithm uses forward checking and combinatorics to try all possible combinations of bridge placements at any given island before moving to the next island, and checks if all constraints are satisfied before terminating. 


## fins3646 - Toolkit for Finance

### project2 - Event Study

*Group Project*

A project that performed some introductory data manipulation using pandas, before using the data to analyse whether total volatility has any effect on a long-short portfolio consisting of top-performing NYSE stocks. The event study was deliberately made unclear for us to figure out the null hypothesis for ourselves. 


## comp2511 - Object-Oriented Design and Programming

### assignment-ii - Dungeon-crawler game

*Pair Project*

Improves upon the current dungeon-crawler game by implementing new features. My partner worked on a logic gates feature. Meanwhile I worked on the snakes feature, which is a new enemy with complex behaviour, and the scoring feature, which accurately calculates score as the game progresses. The focus of this assignment were design patterns and testing in Java. 

### assignment-i - Satellite Simulation
Simulates the orbit of satellites around Jupiter using basic object-oriented programming techniques in Java. The focus of this assignment was streams and debugging in Java. 


## seng2021 - Software Requirements and Design Workshop

*Industry-relevant project*

This course was an entire workshop focusing on requirements and design, where our group built an e-invoicing web service and an API deployed using AWS and swagger to be used by other groups. 

### se2021-23t1-einvoicing-api-h10a-brownie-storage-api

*Group Project*

API which uses PostgreSQL to store relevant e-invoicing data in a storage API. This API was managed using GitHub, documented on Swagger and deployed using AWS Elastic Beanstalk. The storage API was the most popular in terms of usage by other groups in the course, compared to other storage APIs. 

### se2021-23t1-einvoicing-frontend-h10a-brownie-storage-api

*Group Project*

Web service for users to create, manage and send invoices to other users easily and securely. The focus of the web service was on accessibility for visually impaired users (inspired by a true story of a blind accountant). The web services was also managed, documented and deployed using the same industry-grade services as the storage API. I had the opportunity to pitch our service with a live demo to a panel of stakeholders. 


## comp3311 - Database Systems

### ass2 - Pokemon Database
An assignment which uses both Python and SQL to store pokemon data, and uses this data to calculate attack values in a simulated pokemon duel.

### ass1 - Beer Database
An assignment which uses SQL to store and manage data about different kinds of beer produced in/imported to Australia. The focus of this assignment was on basic database techniques including merging tables. 


## seng2011 - Formal Verification workshop

This course was a formal verification course, which used mathematical predicates and Dafny to prove the validity of all internal statements within an algorithm or function (white-box testing). 

One of the assignments involved applying the Dutch Flag Sort algorithm to efficiently sorting a sample of DNA, of which there would be billions and billions of DNA-pairs, and 4 different permuations of these pairs to sort through. In terms of Big-O notation for time complexity, the Dutch Flag Sort algorithm reduces an O(n^2) sorting algorithm to an O(n) sorting algorithm. 


## comp2521 - Data Structures and Algorithms

### ass2 - Pagerank
An assignment which uses graph algorithms such as Dijkstra path find, and advanced mathematical formulas, to calculate the pagerank value for web pages, to assist in recommendation of pages to a user in a search engine. 

### lab07 - Maze Simulation
A lab exercise which tests the effectiveness of Depth First Search and Breadth First Search in solving a maze.

### ass1 - Flight Database
An assignment which uses a tree data structure to perform operations in managing a basic flight database.


