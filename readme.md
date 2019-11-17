# Build Server Protocol example

I had some trouble figuring out how to use BSP with Bloop.
Posting this in case someone may find it helpful whether you use Bloop and Scala or not.
Feel free to make a PR to fix any inaccuracies. I'm pretty new to this stuff.

## Credit
Thanks to @tindzk (Tim Nieradzik), @jvican (Jorge), and @ebenwert (Justin Kaeser) for helping out.

## Tutorial
So far Bloop is the only build server that supports BSP.
Bloop accepts BSP requests using JSON-RPC through a socket file.

Example:
```
bloop bsp --socket tempSocketFile-25.socket
```

Then you can have a JSON RPC client send the requests through that socket file.
There's a java library that does that. bsp4j which uses a Language Server Protocol client lsp4j. https://github.com/build-server-protocol/build-server-protocol/tree/master/bsp4j/


I was told the Scala version bsp4s might be deprecated so don't use it.

If you can't use or don't like Scala or Java there probably is a LSP client in your language you could modify.

### Compile with BSP

I want to compile a Scala project I have in `bsp/` and to do that I did three requests. 
`build/initialize`, `build/initialized`, and `buildTarget/compile`.

There's a few things in the request to build initialize.
There's the capabilities (scala) and the root of the project.
Then some other fields.

```
Request build/initialize InitializeBuildParams [
  rootUri = "file:////Users/syedajafri/dev/millworkspace"
  displayName = "bsp"
  version = "1.3.4"
  bspVersion = "2.0"
  capabilities = BuildClientCapabilities [
    languageIds = SeqWrapper (
      "scala"
    )
  ]
  data = null
]
```
Which gives this response
```
Response InitializeBuildResult [
  displayName = "bloop"
  version = "1.3.4"
  bspVersion = "2.0.0-M4+10-61e61e87"
  capabilities = BuildServerCapabilities [
    compileProvider = CompileProvider [
      languageIds = ArrayList (
        "scala",
        "java"
      )
    ]
    testProvider = TestProvider [
      languageIds = ArrayList (
        "scala",
        "java"
      )
    ]
    runProvider = RunProvider [
      languageIds = ArrayList (
        "scala",
        "java"
      )
    ]
    inverseSourcesProvider = true
    dependencySourcesProvider = true
    resourcesProvider = true
    buildTargetChangedProvider = false
  ]
  data = null
]
```

Now that the session is initialized I can start making compilation requests.
So I can call buildTarget/compile and I need to pass in a BuildTargetIdentifier.
Which in this case has the root of the project and an id that is specific to bloop. Which I will explain later.
```
Request buildTarget/compile CompileParams [
  targets = SeqWrapper (
    BuildTargetIdentifier [
      uri = "file:////Users/syedajafri/dev/millworkspace?id=bsp"
    ]
  )
  originId = null
  arguments = null
]

```


At this point your build client will starts receiving notifications about the progress of your build.
e.g. `build/taskStart`, `build/publishDiagnostics`, `build/taskFinish`

`build/taskFinish` gives the following response

```
[
  taskId = TaskId [
    id = "1"
    parents = null
  ]
  eventTime = 1573953184966
  message = "Compiled 'bsp'"
  status = OK
  dataKind = "compile-report"
  data = {"target":{"uri":"file:/Users/syedajafri/dev/millworkspace/bsp/?id=bsp"},"originId":null,"errors":0,"warnings":0,"time":null}
]
```

All done.

## Run the project

Install bloop and mill
https://scalacenter.github.io/bloop/setup#homebrew
https://www.lihaoyi.com/mill/

Then run:
```
mill mill.scalalib.GenIdea/idea #Optional if you want to view it in IntelliJ
mill mill.contrib.Bloop/install
mill bsp.run
```

You'll see a bunch of requests, responses, and notifications on the build.

What did I just do?

Bloop allows you to export a build from mill. `mill mill.contrib.Bloop/install`
and it creates `.bloop/bsp.json`.
This contains some information that bloop uses for understanding how to compile this project.
This is also where the identifer comes from `bsp` that I used in the compile request.

You can run `bloop compile bsp` and I believe it would use BSP to build this project.

But we're interested in figuring out how to build the client ourselves.
bsp/src/Hello.scala does that

I use ammonite to spawn a subprocess that runs bloop and creates the socketfile `tempSocketFile-[some number].socket`.
Using bsp4j I implement a simple build client that just logs requests.
Have the client point to the socket file.
Then I make the 3 JSON-rpc calls noted earlier.

