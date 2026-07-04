package nl.ulso.vmc.directory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.MapBasedEntityRepository;

import java.util.Optional;

@Singleton
final class DefaultTeamRepository
    extends MapBasedEntityRepository<String, Team>
    implements TeamRepository
{
    @Inject
    DefaultTeamRepository()
    {
    }

    @Override
    protected Class<Team> entityClass()
    {
        return Team.class;
    }

    @Override
    protected Class<?> repositoryClass()
    {
        return TeamRepository.class;
    }

    @Override
    protected String entityKeyFrom(Team team)
    {
        return team.name();
    }

    @Override
    public Optional<Team> teamNamed(String name)
    {
        return Optional.ofNullable(map().get(name));
    }
}
