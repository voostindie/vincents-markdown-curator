function run(argv) {
    let name = argv.length > 0 ? argv[0] : "world";
    return JSON.stringify({message: "Hello, " + name});
}