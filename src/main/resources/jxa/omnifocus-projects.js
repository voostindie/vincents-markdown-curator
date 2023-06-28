function run(argv) {
    const folderName = argv[0];
    const projects = Application('OmniFocus')
        .defaultDocument()
        .folders
        .byName(folderName)
        .flattenedProjects
        .whose({
            _match: [ObjectSpecifier().status, 'active']
        });
    const ids = projects.id();
    const names = projects.name();
    let i = 0;
    const items = ids.map(function (id) {
        return {
            id: id,
            name: names[i++]
        };
    });
    return JSON.stringify(items);
}