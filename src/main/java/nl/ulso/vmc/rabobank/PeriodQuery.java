package nl.ulso.vmc.rabobank;

import nl.ulso.markdown_curator.journal.Journal;
import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.*;

import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.*;

import static java.util.Comparator.comparing;

public class PeriodQuery
        implements Query
{
    static final String DEFAULT_FOLDER = "Projects";
    private final Journal model;
    private final QueryResultFactory resultFactory;


    @Inject
    PeriodQuery(Journal model, QueryResultFactory resultFactory)
    {
        this.model = model;
        this.resultFactory = resultFactory;
    }

    @Override
    public String name()
    {
        return "period";
    }

    @Override
    public String description()
    {
        return "Generates an overview of notes touched in a certain period, " +
               "extracted from the journal";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of(
                "start", "First date to include in the period, in YYYY-MM-DD format",
                "duration", "Number of days in the period, defaults to 7",
                "folder", "Folder of notes to report on; defaults to '" + DEFAULT_FOLDER + "'"
        );
    }

    @Override
    public final QueryResult run(QueryDefinition definition)
    {
        LocalDate start = resolveStartDate(definition);
        if (start == null)
        {
            return resultFactory.error("No valid start date specified");
        }
        var duration = resolveDuration(definition);
        if (duration < 0)
        {
            return resultFactory.error("Invalid duration specified");
        }
        var documentNames = model.referencedDocumentsIn(start, duration);
        var folder = definition.configuration().string("folder", DEFAULT_FOLDER);
        var finder = new DocumentFinder(documentNames, folder);
        model.vault().accept(finder);
        return resultFactory.unorderedList(
                finder.selectedDocuments.stream()
                        .sorted(comparing(Document::sortableTitle))
                        .map(Document::link)
                        .toList());
    }

    protected LocalDate resolveStartDate(QueryDefinition definition)
    {
        return definition.configuration().date("start", null);
    }

    protected int resolveDuration(QueryDefinition definition)
    {
        return definition.configuration().integer("duration", 7);
    }

    private static class DocumentFinder
            extends BreadthFirstVaultVisitor
    {
        private final Set<String> documentNames;
        private final String selectedFolderName;
        private final Set<Document> selectedDocuments;

        DocumentFinder(Set<String> documentNames, String folderName)
        {
            this.documentNames = documentNames;
            this.selectedFolderName = folderName;
            selectedDocuments = new HashSet<>();
        }

        @Override
        public void visit(Vault vault)
        {
            vault.folder(selectedFolderName).ifPresent(folder -> folder.accept(this));
        }

        @Override
        public void visit(Document document)
        {
            if (documentNames.contains(document.name()))
            {
                selectedDocuments.add(document);
            }
        }
    }
}
