This is a simple web app designed to download historical forex data from Oanda (a forex broker).  I was planning to host this live for people to use but, at the time of writing, the only decent free hosting option is with Render.  However, their free servers shut down after approximately 15 minutes of inactivty and can take up to 1 minute to spin up when new requests are received, which is not ideal.  Therefore, if you want to use this tool, you will need to run it locally; please follow the instructions below.

General items to be aware of:
- The front end (i.e., web page) is built with Angular (20.3.7).
- The back end (i.e., server that retrieves data from Oanda) is built with the Scala Play Framework (3.0.9)

The server code, located in the forex-server directory, must be running in order for the app to work.  The Scala build tool (sbt) is used to run the server with the following command: <em>sbt run</em> .

The front end code is located in the forex-frontend directory and can be run with the following command: <em>ng serve</em> .  The UI can be accessed at http://localhost:4200/.