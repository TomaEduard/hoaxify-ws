# Hoaxify - Full Stack Application with Spring Boot and React

## Running the Application

- http://hoaxify-frontend.s3-website.eu-west-3.amazonaws.com/#/

- Run `package com.hoaxify.hoaxify;.HoaxifyApplication` as a Java Application. Check Authentication and REST API Sections for executing REST APIs.
- The project presents 2 development environments, one of development and one of production. These are set from the configuration file before starting `application.yml` and
 sending emails from `package com.hoaxify.hoaxify.shared.AmazonSES`. URL+PORT `http://localhost:3000/` and for production at `http://hoaxify-frontend.s3-website.eu-west-3.amazonaws.com` or what environment you have hosted.


- React Application - Import `https://github.com/TomaEduard/hoaxify-frontend` into Visual Studio Code. The development environment is set using the constant located in `seci api \ apiCalls.js`. Here we have 2 constants, one of production and one of test, one comments what is not used and changes and poxy offered by webpack located in `package.json` with the content of the variable used.
- Run `yarn install` followed by `yarn start`
- http://localhost:4200/ with credentials email: admin@hoJaxify.com password: P4ssward

## H2 Console in dev environment

- http://localhost:3000/h2-console
- Use `jdbc:h2:mem:testdb` as JDBC URL 

