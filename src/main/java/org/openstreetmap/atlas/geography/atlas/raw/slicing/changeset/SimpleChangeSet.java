package org.openstreetmap.atlas.geography.atlas.raw.slicing.changeset;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openstreetmap.atlas.geography.atlas.raw.sectioning.TagMap;
import org.openstreetmap.atlas.geography.atlas.raw.temporary.TemporaryEntity;
import org.openstreetmap.atlas.geography.atlas.raw.temporary.TemporaryLine;
import org.openstreetmap.atlas.geography.atlas.raw.temporary.TemporaryPoint;

/**
 * Records any additions, updates and deletions that occurred during point and way raw Atlas
 * slicing. We try to keep this class as light-weight as possible. For additions, we rely on the
 * {@link TemporaryEntity} objects to keep track of the bare minimum needed to create Atlas
 * entities. For tag updates, we store a mapping between the identifier of the object and the added
 * tags. For deletions, we store the set of identifiers to delete. We also keep a mapping between
 * deleted and created lines to properly update any relations. No relation slicing is done with this
 * class. We only update existing relations if a member line was deleted and split into two or more
 * lines.
 *
 * @author mgostintsev
 */
public class SimpleChangeSet
{
    // Points
    private final Set<TemporaryPoint> createdPoints;
    private final Map<Long, TagMap> updatedPointTags;
    private final Set<Long> deletedPoints;

    // Lines
    private final Set<TemporaryLine> createdLines;
    private final Map<Long, TagMap> updatedLineTags;

    // Lines that were sliced will be deleted and replaced by two or more new line segments. We need
    // to maintain this mapping to maintain relation integrity by removing deleted line members and
    // replacing them with the created sliced segments.
    private final Map<Long, Set<Long>> deletedToCreatedLineMapping;

    private final Map<Long, Boolean> createdLinesToClockwiseMapping;

    public SimpleChangeSet()
    {
        this.createdPoints = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.updatedPointTags = new ConcurrentHashMap<>();
        this.deletedPoints = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.createdLines = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.updatedLineTags = new ConcurrentHashMap<>();
        this.deletedToCreatedLineMapping = new ConcurrentHashMap<>();
        this.createdLinesToClockwiseMapping = new ConcurrentHashMap<>();
    }

    public void createDeletedToCreatedMapping(final long deletedIdentifier,
            final Set<Long> createdIdentifiers)
    {
        this.deletedToCreatedLineMapping.put(deletedIdentifier, createdIdentifiers);
    }

    public void createLine(final TemporaryLine line)
    {
        this.createdLines.add(line);
    }

    public void createLine(final TemporaryLine line, final boolean clockwise)
    {
        this.createdLines.add(line);
        this.createdLinesToClockwiseMapping.put(line.getIdentifier(), clockwise);
    }

    public void createPoint(final TemporaryPoint point)
    {
        this.createdPoints.add(point);
    }

    public void deletePoint(final long identifier)
    {
        this.deletedPoints.add(identifier);
    }

    public Map<Long, Boolean> getClockwiseMapping()
    {
        return this.createdLinesToClockwiseMapping;
    }

    public Set<TemporaryLine> getCreatedLines()
    {
        return this.createdLines;
    }

    public Set<TemporaryPoint> getCreatedPoints()
    {
        return this.createdPoints;
    }

    public Set<Long> getDeletedPoints()
    {
        return this.deletedPoints;
    }

    public Map<Long, Set<Long>> getDeletedToCreatedLineMapping()
    {
        return this.deletedToCreatedLineMapping;
    }

    public Map<Long, TagMap> getUpdatedLineTags()
    {
        return this.updatedLineTags;
    }

    public Map<Long, TagMap> getUpdatedPointTags()
    {
        return this.updatedPointTags;
    }

    public void updateLineTags(final long lineIdentifier, final Map<String, String> newTags)
    {
        this.updatedLineTags.put(lineIdentifier, new TagMap(newTags));
    }

    public void updatePointTags(final long pointIdentifier, final Map<String, String> newTags)
    {
        this.updatedPointTags.put(pointIdentifier, new TagMap(newTags));
    }
}
