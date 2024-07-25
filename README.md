# Vincent's Markdown Curator - vmc

This is a Java application on top of my [markdown-curator](https://github.com/voostindie/markdown-curator) framework that contains all the specifics for my own Markdown repositories (Obsidian vaults). I have 4 of them:

1. Rabobank (work)
2. TweeVV (volunteering)
3. Personal (personal)
4. Demo

The code in this repository is not private; it only contains queries on top of my content, not the content itself. 

That also means the code is pretty much worthless to anybody, except maybe that it can serve as an example of what can be done with the `markdown-curator` framework. Which is why I made it public anyway.

In a perfectly modular world each curator would be in its own JAR. I haven't done that simply because it leads to more maintainance - at least 5 JARs to release separately, in the right order: the core curator, some other code shared between the curators, and each curator itself - and I have no need to run the curators separately anyway. I prefer to spin up a single Java application that runs in the background all day long over having to run multiple of them, or run each one only when needed.
