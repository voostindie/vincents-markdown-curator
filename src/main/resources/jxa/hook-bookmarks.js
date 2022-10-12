function run(argv) {
    let hook = Application('Hook');
    let documentUrl = argv[0];
    let documentBookmark = hook.Bookmark({address: documentUrl}).make();
    let bookmarks = documentBookmark.hookedBookmarks()
        .map(function (bookmarkFunction) {
            // Sometimes hooks "break" and become invalid. This happens for
            // example when the file a hook points to is moved around on disk.
            // Those throw an exception when accessed using scripting.
            try {
                return bookmarkFunction();
            } catch {
                return null;
            }
        })
        .filter(function (bookmark) {
            return bookmark != null
        })
        .map(function (bookmark) {
            return {
                name: bookmark.name(),
                address: bookmark.address()
            };
        });
    return JSON.stringify(bookmarks);
}