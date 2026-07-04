package nl.ulso.vmc.backlink;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.ChangeCollector;
import nl.ulso.curator.change.EntityProcessor;
import nl.ulso.curator.vault.Document;
import nl.ulso.curator.vault.InternalLink;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toCollection;

/// Produces backlinks to documents from other documents, but only for the documents that are
/// tracked by the [BacklinkQueryReferenceRepository].
///
/// Backlinks are only created and deleted, never updated.
@Singleton
final class BacklinkProducer
    extends EntityProcessor<Document>
{
    private final BacklinkQueryReferenceRepository repository;

    @Inject
    BacklinkProducer(BacklinkQueryReferenceRepository repository)
    {
        this.repository = repository;
    }

    @Override
    protected Class<Document> entityClass()
    {
        return Document.class;
    }

    @Override
    public Set<Class<?>> producedPayloadTypes()
    {
        return Set.of(Backlink.class);
    }

    @Override
    public Set<Class<?>> requiredPayloadTypes()
    {
        return Set.of(BacklinkQueryReferenceRepository.class);
    }

    @Override
    protected void entityCreated(Document newDocument, ChangeCollector collector)
    {
        findBacklinks(newDocument).forEach(link ->
            collector.create(new Backlink(link, newDocument.name()), Backlink.class));
    }

    @Override
    protected void entityUpdated(
        Document oldDocument, Document newDocument, ChangeCollector collector)
    {
        var oldLinks = findBacklinks(oldDocument);
        var newLinks = findBacklinks(newDocument);
        if (!oldLinks.isEmpty())
        {
            var deletedLinks = new HashSet<>(oldLinks);
            oldLinks.removeAll(newLinks);
            deletedLinks.forEach(
                link -> collector.delete(new Backlink(link, oldDocument.name()), Backlink.class));
        }
        if (!newLinks.isEmpty())
        {
            var createdLinks = new HashSet<>(newLinks);
            createdLinks.removeAll(oldLinks);
            createdLinks.forEach(
                link -> collector.create(new Backlink(link, newDocument.name()), Backlink.class));
        }
    }

    @Override
    protected void entityDeleted(Document oldDocument, ChangeCollector collector)
    {
        findBacklinks(oldDocument).forEach(link ->
            collector.delete(new Backlink(link, oldDocument.name()), Backlink.class));
    }

    private Set<String> findBacklinks(Document document)
    {
        var backlinkDocumentNames = repository.backlinkDocumentNames();
        if (backlinkDocumentNames.isEmpty())
        {
            return emptySet();
        }
        var links = document.findInternalLinks().stream()
            .map(InternalLink::targetDocument)
            .collect(toCollection(HashSet::new));
        links.retainAll(backlinkDocumentNames);
        return links;
    }
}
