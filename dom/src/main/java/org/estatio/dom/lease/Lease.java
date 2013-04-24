package org.estatio.dom.lease;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.VersionStrategy;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import org.estatio.dom.EstatioTransactionalObject;
import org.estatio.dom.asset.Unit;
import org.estatio.dom.party.Party;
import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Render;
import org.apache.isis.applib.annotation.Render.Type;
import org.apache.isis.applib.annotation.Title;
import org.apache.isis.objectstore.jdo.applib.service.support.IsisJdoSupport;

@PersistenceCapable
@javax.jdo.annotations.Version(strategy = VersionStrategy.VERSION_NUMBER, column = "VERSION")
public class Lease extends EstatioTransactionalObject implements Comparable<Lease> {

    // {{ Reference (property)
    private String reference;

    @MemberOrder(sequence = "1")
    @Title()
    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    // }}

    // {{ Derived attribute

    @MemberOrder(sequence = "2")
    @Disabled
    @Optional
    public Party getCurrentLandlord() {
        // TODO:test to see if this is faster:
        // leaseActors.findLeaseActorWithType(this, LeaseActorType.LANDLORD,
        // LocalDate.now())
        return firstElseNull(LeaseActorType.LANDLORD);
    }

    @MemberOrder(sequence = "3")
    @Disabled
    @Optional
    public Party getCurrentTenant() {
        // TODO:test to see if this is faster:
        // leaseActors.findLeaseActorWithType(this, LeaseActorType.LANDLORD,
        // LocalDate.now())
        return firstElseNull(LeaseActorType.TENANT);
    }

    private Party firstElseNull(LeaseActorType lat) {
        Iterable<Party> parties = Iterables.transform(Iterables.filter(getActorsWorkaround(), currentLeaseActorOfType(lat)), partyOfLeaseActor());
        return firstElseNull(parties);
    }

    public static <T> T firstElseNull(Iterable<T> elements) {
        Iterator<T> iterator = elements.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    private Function<LeaseActor, Party> partyOfLeaseActor() {
        return new Function<LeaseActor, Party>() {
            public Party apply(LeaseActor la) {
                return la.getParty();
            }
        };
    }

    private static Predicate<LeaseActor> currentLeaseActorOfType(final LeaseActorType lat) {
        return new Predicate<LeaseActor>() {
            public boolean apply(LeaseActor candidate) {
                return candidate.getType() == lat && candidate.isCurrent();
            }
        };
    }

    // }}

    // {{ Name (property)
    private String name;

    @MemberOrder(sequence = "4")
    @Optional
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    // }}

    // {{ StartDate (property)
    private LocalDate startDate;

    @Persistent
    @MemberOrder(sequence = "5")
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }

    // }}

    // {{ EndDate (property)
    private LocalDate endDate;

    @Persistent
    @MemberOrder(sequence = "6")
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    // }}

    // {{ TerminationDate (property)
    private LocalDate terminationDate;

    @Persistent
    @MemberOrder(sequence = "7")
    @Optional
    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(final LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }

    // }}

    // {{ Type (property)
    private LeaseType type;

    @MemberOrder(sequence = "8")
    public LeaseType getType() {
        return type;
    }

    public void setType(final LeaseType type) {
        this.type = type;
    }

    // }}

    // {{ PreviousLease (property)
    private Lease previousLease;

    @Persistent(mappedBy = "nextLease")
    @Disabled
    @Optional
    @MemberOrder(sequence = "9")
    public Lease getPreviousLease() {
        return previousLease;
    }

    public void setPreviousLease(final Lease previousLease) {
        this.previousLease = previousLease;
    }

    public void modifyPreviousLease(Lease lease) {
        this.setPreviousLease(lease);
        lease.setNextLease(this); // not strictly necessary, as JDO will also do
                                  // this (bidir link)
    }

    // }}

    // {{ NextLease (property)
    private Lease nextLease;

    @Disabled
    @Optional
    @MemberOrder(sequence = "10")
    public Lease getNextLease() {
        return nextLease;
    }

    public void setNextLease(final Lease nextLease) {
        this.nextLease = nextLease;
    }

    public void modifyNextLease(Lease lease) {
        this.setNextLease(lease);
        lease.setPreviousLease(this); // not strictly necessary, as JDO will
                                      // also do this (bidir link)
    }

    public void clearNextLease() {
        Lease nextLease = getNextLease();
        if (nextLease != null) {
            nextLease.setPreviousLease(null);
            setNextLease(null);
        }
    }

    // }}

    // {{ Actors (Collection)
    private SortedSet<LeaseActor> actors = new TreeSet<LeaseActor>();

    @Persistent(mappedBy = "lease")
    @MemberOrder(name = "Actors", sequence = "11")
    @Render(Type.EAGERLY)
    public SortedSet<LeaseActor> getActors() {
        return actors;
    }

    @Hidden
    public SortedSet<LeaseActor> getActorsWorkaround() {

        if (getActors() == null) {
            return null;
        } else {
            if (isisJdoSupport == null) {
                return getActors(); // otherwise I have to inject this in every
                                    // single unit test.
            } else {
                // inject each element before returning it
                return Sets.newTreeSet(Iterables.transform(getActors(), new Function<LeaseActor, LeaseActor>() {
                    public LeaseActor apply(LeaseActor leaseActor) {
                        return isisJdoSupport.injected(leaseActor);
                    }
                }));
            }
        }

    }

    public void setActors(final SortedSet<LeaseActor> actors) {
        this.actors = actors;
    }

    public void addToActors(final LeaseActor actor) {
        // check for no-op
        if (actor == null || getActorsWorkaround().contains(actor)) {
            return;
        }
        // dissociate arg from its current parent (if any).
        actor.clearLease();
        // associate arg
        actor.setLease(this);
        getActors().add(actor);
        // additional business logic
        // onAddToActors(actor);
    }

    public void removeFromActors(final LeaseActor actor) {
        // check for no-op
        if (actor == null || !getActorsWorkaround().contains(actor)) {
            return;
        }
        // dissociate arg
        actor.setLease(null);
        getActors().remove(actor);
        // additional business logic
        // onRemoveFromActors(actor);
    }
    
    @MemberOrder(name = "Actors", sequence = "11")
    public LeaseActor addActor(@Named("party") Party party, @Named("type") LeaseActorType type, @Named("startDate") @Optional LocalDate startDate, @Named("endDate") @Optional LocalDate endDate) {
        if (party == null || type == null)
            return null;
        LeaseActor leaseActor = findActor(party, type, startDate);
        if (leaseActor == null) {
            leaseActor = leaseActors.newLeaseActor(this, party, type, startDate, endDate);
        }
        leaseActor.setEndDate(endDate);
        return leaseActor;
    }

    // {{ Units (Collection)
    private SortedSet<LeaseUnit> units = new TreeSet<LeaseUnit>();

    @Persistent(mappedBy = "lease")
    @MemberOrder(name = "Units", sequence = "20")
    @Render(Type.EAGERLY)
    public SortedSet<LeaseUnit> getUnits() {
        return units;
    }

    public void setUnits(final SortedSet<LeaseUnit> units) {
        this.units = units;
    }

    public void addToUnits(final LeaseUnit leaseUnit) {
        // check for no-op
        if (leaseUnit == null || getUnits().contains(leaseUnit)) {
            return;
        }
        // associate new
        getUnits().add(leaseUnit);
        // additional business logic
        onAddToUnits(leaseUnit);
    }

    public void removeFromUnits(final LeaseUnit leaseUnit) {
        // check for no-op
        if (leaseUnit == null || !getUnits().contains(leaseUnit)) {
            return;
        }
        // dissociate existing
        getUnits().remove(leaseUnit);
        // additional business logic
        onRemoveFromUnits(leaseUnit);
    }

    protected void onAddToUnits(final LeaseUnit leaseUnit) {
    }

    protected void onRemoveFromUnits(final LeaseUnit leaseUnit) {
    }

    @MemberOrder(name = "Units", sequence = "21")
    public LeaseUnit addUnit(@Named("unit") Unit unit) {
        LeaseUnit leaseUnit = leaseUnits.newLeaseUnit(this, unit);
        units.add(leaseUnit);
        return leaseUnit;
    }

    // }}

    // {{ Items (Collection)
    private SortedSet<LeaseItem> items = new TreeSet<LeaseItem>();

    @Render(Type.EAGERLY)
    @Persistent(mappedBy = "lease")
    @MemberOrder(name = "Items", sequence = "30")
    public SortedSet<LeaseItem> getItems() {
        return items;
    }

    public void setItems(final SortedSet<LeaseItem> items) {
        this.items = items;
    }

    @MemberOrder(name = "Items", sequence = "31")
    public LeaseItem newItem(LeaseItemType type) {
        LeaseItem leaseItem = leaseItems.newLeaseItem(this, type);
        return leaseItem;
    }

    @Hidden
    public LeaseActor findActor(Party party, LeaseActorType type, LocalDate startDate) {
        return leaseActors.findLeaseActor(this, party, type, startDate, startDate);
    }

    @Hidden
    public LeaseActor findActorWithType(LeaseActorType leaseActorType, LocalDate date) {
        return leaseActors.findLeaseActorWithType(this, leaseActorType, date);
    }

    @Hidden
    public LeaseItem findItem(LeaseItemType type, LocalDate startDate, BigInteger sequence) {
        // TODO: better/faster filter options? -> Use predicate
        for (LeaseItem item : getItems()) {
            LocalDate itemStartDate = item.getStartDate();
            LeaseItemType itemType = item.getType();
            if (itemType.equals(type) && itemStartDate.equals(startDate) && item.getSequence().equals(sequence)) {
                return item;
            }
        }
        return null;
    }

    // }}

    // {{ Actions
    @Bulk
    public Lease verify() {
        for (LeaseItem item : getItems()) {
            item.verify();
        }
        return this;
    }

    @Bulk
    public Lease calculate(@Named("Due date") LocalDate dueDate) {
        // TODO: I know that bulk actions only appear whith a no-arg but why
        // not?
        for (LeaseItem item : getItems()) {
            item.calculate(dueDate);
        }
        return this;
    }

    // }}

    @Override
    public int compareTo(Lease other) {
        return this.getReference().compareTo(other.getReference());
    }

    // {{ injected services
    private LeaseItems leaseItems;

    public void setLeaseItems(final LeaseItems leaseItems) {
        this.leaseItems = leaseItems;
    }

    private LeaseUnits leaseUnits;

    public void setLeaseUnits(final LeaseUnits leaseUnits) {
        this.leaseUnits = leaseUnits;
    }

    private LeaseActors leaseActors;

    public void setLeaseActors(final LeaseActors leaseActors) {
        this.leaseActors = leaseActors;
    }

    // }}
    // {{ services
    private IsisJdoSupport isisJdoSupport;

    public void setIsisJdoSupport(IsisJdoSupport isisJdoSupport) {
        this.isisJdoSupport = isisJdoSupport;
    }

}
