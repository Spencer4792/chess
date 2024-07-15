# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```

## URL for link to Chess Server Design

https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2Z0YKAE9VuImgDmMAAwA6AJyZMdqBACu2GADEaMBUFjAASih2SKrmckgQaIEA7gAWSGBiiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9D4GUAA6aADeAETtlMEAtih9pX0wfQA0U7jqydAc45MzUyjDwEgIK1MAvpjCJTAFrOxclOX9g1AjYxNTs33zqotQyw9rfRtbO58HbE43FgpyOonKUCiMUyUAAFJForFKJEAI4+NRgACUh2KohOhVk8iUKnU5TsKDAAFUOrCbndsYTFMo1Kp8UYdOUAGJITgwamURkwHRhOnAUaYRnElknUG4lTlNA+BAIHEiFRsyXM0kwECQuQoflwm6MhnaKXqNnGcoKDgcPkdRmqqh406akmqcq6lD6hQ+MCpWHAP2pE0Ss1a1mnK0wG1233+x1g9W5U6Ai5lCJQpFQSKqJVYNPAmWFI6XGDXDp3SblVZPIP++oQADW6GrU32TsoxfgyDM5QATI5HN0K0MxWMYDXHlN66lGy20G3Vgd0BwPF5fP4AtB2OSYAAZCDRJIBNIZLK9vIl4pl6p1JqtAzqBJoEcDSvjlZfF5vD59PoHKWIKFIWZajrcn4TN++ivEsX4AucRaymqKDlAgR48rCh7Hqi6KxNiSaGK64bumSFKGrSH6jKaRIRpaHIwAAiuilgwDcmg6N2bosuUhqOtxFopteKG8Q62hOi6wnOvKMAlEgABmYQ-ksEnJgSJE8TqeqZPGAaitRYa0e69HlAAkmgHTMLOHFcRp2q6fxdmRsh0moTGwaJnKRGpohZbYTyub5pgoEgi5YHvmOoxfnWwbzq2UGATeoWFNkfYwIOw69BFEFRVBMUNs28X-iunDrt4fiBF4KDoAeR6+Mwp7pJkmCpVeRTULe0gAKL7l19Rdc0LRPqoL7dLOcVoJ2wFnECZbjYVk0hWyhFoXVfqBrFC0EV5GpOWRYC6RtBULjRTLGVGDHMZQYTWcYtlGZpDniQJzlSeC7kJuJhG7Q92ocCg3A6cGR1zlthlndKF3lNIAMUoYt2cUJMh7TAeYRuD5qve1rnlGj7qqd5IG+Rm2H1YFCAFsT3ZAVciUdclPY5GAA5DiOJVrp45VbpCdr7tCMAAOLjqyjXni1l7MGFGYVALfWDXY45jZtC5TWyIXlPNKtLS573ILEQujKoIMTdtKE-RD2rkgdwOa+gp2YyZTEsTdwY2UjL3Wh5z1OdTXme598gE+bmNodCBtqLC9t0VDTvXTAwDKjACuG27xG-R6B7QgAPOHjL5Bj0c6zJfOxDn4550HSPq5n+vC+TlOzQzNPln0ydqOMlTOF3jR08cSOtcz6Ws1lbeqB3FRd84PemKVnOboE2A+FA2DcPA2mGOHKRNReTPLUl5R3g08uK8EyvoG+bcAHKQcVqs+Y3Gtn2gsz9FfN8AcFVNF25Xr6uHxsLVmG-Ay3004WwzlbQ6ts0BR3OoUaMV1WII3uuA-2IZvbp19iJD66DA6gPUunT069-7AJQLAyG8CGLmUsknccqcCGoMFuXDB4CsE4yYaMTyZsq7E3KHAYh4566f0bmw8KbdTLSA7gARn7AAZgACy9y7P3CWLNMqv3HBI6RcjFEzw5huCqAQLAA3QskGAAApCAPIOGGACDoBAoAmzi13lLA+VRKQPhaG3JWx1z5ZRXsAYxUA4AQHQlAaKUxxGSNKDIhRSjprV2gS-Z4DigkhLCRE1umjomxN0drN6MkABWVi0D-ySbQ0YEjZgBLSaE6AptXLBwjPtKBT9yGCUoeURBLt-T0ORoQnBjlMFIxWoMr6O0wEhyThSEh2T2mRk6bHVibc+kexsUM1hIy-brPGdwgpbkqAJyQFoTIMBinWMhGiDElcGFTL8CclAszKnSFmPpFA1TUmUHSfUgucD2S8WwA8ipYg7ruxRnjFkvzIbf3KPc-Udpc67MaTwh+FiSnhyEdrVx5Z4ndgHmotmeiyrzwCF4QJPZvSwGANgFehB4iJC3mLAee96YH26r1fqg1jB3yJqi1IvlYSXLwliYR6YWXsJANwPACgaXIBAPStAkcbn9MYZKylSqXqO0QJS+OicTnAFWeCnw6NNVbOwRC9Qld77pj4VKnMaggpYqkmBXFKimYEt6OzIAA
