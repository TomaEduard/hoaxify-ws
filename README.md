## Running the Application

- http://hoaxify-frontend.s3-website.eu-west-3.amazonaws.com/#/

- Run `package com.hoaxify.hoaxify.HoaxifyApplication` as a Java Application. Check Authentication and REST API Sections for executing REST APIs.
- The project presents 2 development environments, one of development and one of production. These are set from the configuration file before starting `application.yml` and
 sending emails from `package com.hoaxify.hoaxify.shared.AmazonSES`. URL+PORT `http://localhost:3000/` and for production at `http://hoaxify-frontend.s3-website.eu-west-3.amazonaws.com` or what environment you have hosted.

# Front end code

- React Application - Import `https://github.com/TomaEduard/hoaxify-frontend` into Visual Studio Code. The development environment is set using the constant located in `seci api \ apiCalls.js`. Here we have 2 constants, one of production and one of test, one comments what is not used and changes and poxy offered by webpack located in `package.json` with the content of the variable used.
- Run `yarn install` followed by `yarn start`, it also works with `npm install` and `npm start`
- http://localhost:4200/ with credentials email: admin@hoaxify.com password: P4ssward

## H2 Console in dev environment

- http://localhost:3000/h2-console
- Use `jdbc:h2:mem:testdb` as JDBC URL 

# Screenshots
1. Login Page

![Login Page](https://github.com/TomaEduard/hoaxify-ws/blob/master/src/main/resources/images/login.png)

2. SignUp Page

![SignUp Page](https://github.com/TomaEduard/hoaxify-ws/blob/master/src/main/resources/images/signup.png)

3. Feed Tab

![Feed Tab](https://github.com/TomaEduard/hoaxify-ws/blob/master/src/main/resources/images/feed.png)

4. Preferences Tab

![Preferences Tab](https://github.com/TomaEduard/hoaxify-ws/blob/master/src/main/resources/images/preferences.png)

5. Explor Tab

![Explor Tab](https://github.com/TomaEduard/hoaxify-ws/blob/master/src/main/resources/images/explor.png)

6. Profile Page

![Profile Page](https://github.com/TomaEduard/hoaxify-ws/blob/master/src/main/resources/images/profile.png)

7. Profile Page - Security TAb

![Profile Page - Security TAb](https://github.com/TomaEduard/hoaxify-ws/blob/master/src/main/resources/images/profile-security.png)