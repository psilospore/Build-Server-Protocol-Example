# Build Server Protocol example

I had some trouble figuring out how to use BSP with Bloop.
Posting this in case someone may find it helpful whether you use Bloop and Scala or not.
Feel free to make a PR to fix any inaccuracies.

Thanks to @tindzk and Justin for helping out.

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

### Compile

I want to compile `bsp/` and to do that I did three requests. 
`build/initialize`, `build/initialized`, and `buildTarget/compile`.

InitializeBuildParams


### Run this

Install bloop and mill
https://scalacenter.github.io/bloop/setup#homebrew
https://www.lihaoyi.com/mill/

Then run
```
mill mill.scalalib.GenIdea/idea #Optional if you want to view it in IntelliJ
mill bsp.run
```