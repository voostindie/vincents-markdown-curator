package nl.ulso.vmc.bilateral;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.addon.journal.Daily;
import nl.ulso.curator.addon.journal.Journal;
import nl.ulso.curator.change.*;
import nl.ulso.curator.vault.Vault;

import java.time.LocalDate;
import java.util.*;

import static nl.ulso.curator.change.Change.*;
import static nl.ulso.curator.change.ChangeHandler.newChangeHandler;
import static nl.ulso.vmc.bilateral.BilateralRegistryUpdate.BILATERAL_REGISTRY_UPDATE;

/// Keeps track of bilateral meetings in the journal for all counterparts.
///
/// A bilateral meeting is recognized if a line in the daily starts with [#BILATERAL_PREFIX] and
/// that same line links to a counterpart.
@Singleton
final class DefaultBilateralMeetingRegistry
    extends ChangeProcessorTemplate
    implements BilateralMeetingRegistry
{
    private final CounterpartRegistry counterpartRegistry;
    private final Journal journal;
    private final Map<String, SortedSet<LocalDate>> meetings;

    @Inject
    DefaultBilateralMeetingRegistry(CounterpartRegistry counterpartRegistry, Journal journal)
    {
        this.counterpartRegistry = counterpartRegistry;
        this.journal = journal;
        this.meetings = new HashMap<>();
    }

    @Override
    public Set<Class<?>> consumedPayloadTypes()
    {
        return Set.of(Vault.class, Counterpart.class, Daily.class);
    }

    @Override
    public Set<Class<?>> producedPayloadTypes()
    {
        return Set.of(BilateralRegistryUpdate.class);
    }

    @Override
    protected Collection<Change<?>> createChangeCollection()
    {
        return new HashSet<>(1);
    }

    @Override
    protected Set<? extends ChangeHandler> createChangeHandlers()
    {
        return Set.of(
            newChangeHandler(
                isPayloadType(Counterpart.class).and(isCreate()),
                this::counterpartCreated
            ),
            newChangeHandler(
                isPayloadType(Counterpart.class).and(isUpdate()),
                this::counterpartUpdated
            ),
            newChangeHandler(
                isPayloadType(Counterpart.class).and(isDelete()),
                this::counterpartDeleted
            ),
            newChangeHandler(
                isPayloadType(Daily.class).and(isCreate()),
                this::dailyCreated
            ),
            newChangeHandler(
                isPayloadType(Daily.class).and(isUpdate()),
                this::dailyUpdated
            ),
            newChangeHandler(
                isPayloadType(Daily.class).and(isDelete()),
                this::dailyDeleted
            )
        );
    }

    @Override
    protected void reset(ChangeCollector collector)
    {
        meetings.clear();
        counterpartRegistry.counterparts().forEach(counterpart ->
            counterpartCreated(create(counterpart, Counterpart.class), collector));
    }

    private void counterpartCreated(Change<?> change, ChangeCollector collector)
    {
        var counterpart = change.as(Counterpart.class).value();
        var documentName = counterpart.document().name();
        meetings.put(documentName, new TreeSet<>());
        meetings.get(documentName).add(FALLBACK_NEVER);
        journal.dailiesFor(documentName).forEach(daily ->
        {
            if (hasBilateralMeetingWith(counterpart, daily))
            {
                meetings.get(documentName).add(daily.date());
            }
        });
        collector.add(BILATERAL_REGISTRY_UPDATE);
    }

    private void counterpartUpdated(Change<?> change, ChangeCollector collector)
    {
        collector.add(BILATERAL_REGISTRY_UPDATE);
    }

    private void counterpartDeleted(Change<?> change, ChangeCollector collector)
    {
        meetings.remove(change.as(Counterpart.class).value().document().name());
        collector.add(BILATERAL_REGISTRY_UPDATE);
    }

    private void dailyCreated(Change<?> change, ChangeCollector collector)
    {
        var daily = change.as(Daily.class).value();
        counterpartRegistry.counterparts().forEach(counterpart ->
        {
            if (hasBilateralMeetingWith(counterpart, daily))
            {
                meetings.get(counterpart.name()).add(daily.date());
            }
        });
        collector.add(BILATERAL_REGISTRY_UPDATE);
    }

    private void dailyUpdated(Change<?> change, ChangeCollector collector)
    {
        dailyDeleted(change, collector);
        dailyCreated(change, collector);
    }

    private void dailyDeleted(Change<?> change, ChangeCollector collector)
    {
        meetings.forEach((_, dates) ->
            dates.remove(change.as(Daily.class).value().date()));
        collector.add(BILATERAL_REGISTRY_UPDATE);
    }

    private boolean hasBilateralMeetingWith(Counterpart counterpart, Daily daily)
    {
        return daily.refersTo(counterpart.name())
               && daily.linesFor(counterpart.name()).stream()
                   .anyMatch(line -> line.startsWith(BILATERAL_PREFIX));
    }

    @Override
    public Map<Counterpart, LocalDate> resolveBilateralMeetings()
    {
        var counterparts = counterpartRegistry.counterparts();
        var result = new HashMap<Counterpart, LocalDate>(counterparts.size());
        counterparts.forEach(counterpart ->
            result.put(counterpart, meetings.get(counterpart.name()).last()));
        return sortByDate(result);
    }

    public static Map<Counterpart, LocalDate> sortByDate(Map<Counterpart, LocalDate> map)
    {
        var entries = new ArrayList<>(map.entrySet());
        entries.sort(Map.Entry.comparingByValue());
        var result = new LinkedHashMap<Counterpart, LocalDate>(map.size());
        for (var entry : entries)
        {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
