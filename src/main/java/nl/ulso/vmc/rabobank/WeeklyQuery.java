package nl.ulso.vmc.rabobank;

import nl.ulso.markdown_curator.journal.Journal;
import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.*;

import javax.inject.Inject;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.time.LocalDate.now;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR;
import static java.util.Comparator.comparing;
import static java.util.regex.Pattern.compile;

public class WeeklyQuery
        implements Query
{
    private static final Pattern WEEKLY_DOCUMENT_NAME_PATTERN = compile("^(\\d{4}) Week (\\d{2})");
    private static final String DEFAULT_FOLDER = "Projects";
    private final Journal model;
    private final QueryResultFactory resultFactory;

    @Inject
    WeeklyQuery(Journal model, QueryResultFactory resultFactory)
    {
        this.model = model;
        this.resultFactory = resultFactory;
    }

    @Override
    public String name()
    {
        return "weekly";
    }

    @Override
    public String description()
    {
        return "Generates a weekly overview of activities, extracted from the journal";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of(
                "year", "Year of the overview; defaults to the year from the document name",
                "week", "Week of the overview; defaults to the week from the document name",
                "folder", "Folder of notes to report on; defaults to '" + DEFAULT_FOLDER + "'"
        );
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var year = definition.configuration().integer("year", -1);
        var week = definition.configuration().integer("week", -1);
        var folder = definition.configuration().string("folder", DEFAULT_FOLDER);
        if (year == -1 || week == -1)
        {
            var matcher = WEEKLY_DOCUMENT_NAME_PATTERN.matcher(definition.document().name());
            if (matcher.matches())
            {
                year = year != -1 ? year : parseInt(matcher.group(1));
                week = week != -1 ? week : parseInt(matcher.group(2));
            }
            else
            {
                return resultFactory.error("Invalid year and/or week specified");
            }
        }
        LocalDate start;
        try
        {
            start = now().withYear(year).with(WEEK_OF_WEEK_BASED_YEAR, week).with(DAY_OF_WEEK, 1);
        }
        catch (DateTimeException e)
        {
            return resultFactory.error(
                    "Invalid date for year " + year + " and week " + week + ":  " + e.getMessage());
        }
        var documentNames = model.referencedDocumentsIn(start, 7);
        var finder = new DocumentFinder(documentNames, folder);
        model.vault().accept(finder);
        return resultFactory.unorderedList(
                finder.selectedDocuments.stream()
                        .sorted(comparing(Document::sortableTitle))
                        .map(Document::link)
                        .toList());
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
