To setup a system to use this project the following things need to be done.
This assumes using ubuntu, with postgres13.

1. Create a user for this program, we will call it `einvoice`;

    To create this run the follwing command to launch psql

    `sudo -u postgres psql`

    Once in postgres run this command to create the user:

    `CREATE USER einvoice WITH PASSWORD 'password';`

    And create the database:

    `CREATE DATABASE einvoice OWNER einvoice;`

    Then quit psql with `\q`
2. Then add the user on ubuntu with `sudo adduser einvoice`

3. Then setup the databse with `sudo -u einvoice psql einvoice -f database/einvoice.dump`.

Note if you did not use the einvoice user to use the dump file then you will need to give write access to the einvoice user. With the command `GRANT ALL ON table TO einvoice;`