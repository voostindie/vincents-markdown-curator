function run(argv) {
    let documentUrl = argv[0];
    let hook = Application('Hook');
    let bookmark = hook.Bookmark({address: documentUrl}).make();
    let items = bookmark.hookedBookmarks().map(function (bookmark) {
        return {
            name: bookmark().name(),
            address: bookmark().address()
        };
    });
    return JSON.stringify(items);
}