package swarm_sim.perception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AngleSegment implements Comparator<AngleSegment> {
    public double start, end;

    public AngleSegment(double start, double end) {
	this.start = start;
	this.end = end;
    }

    public boolean merge(AngleSegment s) {
	if (start < s.start && end > s.end) {
	    return true; /* Segment overlaps */
	}
	if (s.start < start && s.end > end) {
	    start = s.start; /* other segment overlaps */
	    end = s.end;
	    return true;
	}
	if (s.start < start && s.end > start && s.end < end) {
	    start = s.start;
	    return true;
	}
	if (s.end > end && s.start < end && s.start > start) {
	    end = s.end;
	    return true;
	}
	return false;
    }

    /**
     * Build the difference between the Segment and some filter segments
     * 
     * @param filters
     * @return Remaining segments of the original segment
     */
    public List<AngleSegment> filterSegment(List<AngleSegment> filters) {
	List<AngleSegment> segments = new ArrayList<>();

	Collections.sort(filters, this);

	AngleSegment remaining = new AngleSegment(start, end);

	for (AngleSegment s : filters) {
	    if (s.end <= remaining.start && s.start >= remaining.end)
		continue;

	    if (s.start > s.end) {
		/* Segment passes Math.Pi/-Math.Pi border */
		/* we assume that this (segment) never crosses this border */
		if (s.end >= remaining.end)
		    return segments;
		if (s.end >= remaining.start && s.end <= remaining.end) {
		    remaining.start = s.end;
		    continue;
		}
		if (s.start < remaining.start)
		    return segments;
		if (s.start < remaining.end && s.start > remaining.start) {
		    remaining.end = s.start;
		    continue;
		}
		continue;
	    } else {
		if (s.end <= remaining.start)
		    continue;
		if (s.start <= remaining.start && s.end > remaining.start) {
		    if (s.end <= remaining.end)
			remaining.start = s.end;
		    else
			return segments;
		    continue;
		}
		if (s.start >= remaining.start && s.start < remaining.end) {
		    segments.add(new AngleSegment(remaining.start, s.start));
		    if (s.end > remaining.end) {
			remaining.start = remaining.end;
			return segments;
		    } else {
			remaining.start = s.end;
		    }
		    continue;
		}
	    }
	    System.err.println("Something went wrong here");
	    System.err.println(s.start + "\t" + s.end);
	    System.err.println(remaining.start + "\t" + remaining.end);
	}
	if (remaining.start < remaining.end)
	    segments.add(new AngleSegment(remaining.start, remaining.end));
	// else {
	// System.err.println("remaining.start NOT < remaining.end");
	// System.err.println(remaining.start + "\t" + remaining.end);
	// }
	return segments;
    }

    public List<AngleSegment> calcMutualSegments(List<AngleSegment> filters) {
	List<AngleSegment> segments = new ArrayList<>();
	AngleSegment remaining = new AngleSegment(start, end);

	for (AngleSegment s : filters) {
	    if (s.end <= remaining.start || s.start >= remaining.end)
		continue;

	    if (s.start <= remaining.start && s.end > remaining.start) {
		if (s.end >= remaining.end) {
		    segments.add(remaining);
		    return segments;
		} else {
		    segments.add(new AngleSegment(remaining.start, s.end));
		    remaining.start = s.end;
		}
	    }

	    if (s.start >= remaining.start && s.start < remaining.end) {
		if (s.end > remaining.end) {
		    segments.add(new AngleSegment(s.start, remaining.end));
		    return segments;
		} else {
		    segments.add(new AngleSegment(s.start, s.end));
		    remaining.start = s.end;
		}
	    }
	}

	return segments;
    }

    @Override
    public int compare(AngleSegment a, AngleSegment b) {
	if (a.start > a.end && b.start < b.end)
	    return -1;
	if (b.start > b.end && a.start < b.start)
	    return 1;
	if (a.start < b.start)
	    return -1;
	if (a.start > b.start)
	    return 1;

	return 0;
    }
}