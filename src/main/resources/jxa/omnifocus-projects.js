function run(argv) {
    const folderName = argv[0];
    const projects = Application('OmniFocus')
        .defaultDocument()
        .folders
        .byName(folderName)
        .flattenedProjects
        .whose({
            _or: [
                {_match: [ObjectSpecifier().status, 'active status']},
                {_match: [ObjectSpecifier().status, 'on hold status']}
            ]
        });
    const ids = projects.id();
    const names = projects.name();
    const statuses = projects.status();
    let i = 0;
    const items = ids.map(function (id) {
        return {
            id: id,
            name: names[i],
            status: statuses[i],
            priority: i++
        };
    });
    return JSON.stringify(items);
}