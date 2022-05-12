package nl.ulso.vmc.rabobank;

import nl.ulso.markdown_curator.vault.Section;

import java.time.LocalDate;

record JournalEntry(LocalDate date, String folder, Section section, String subject)
{
}
