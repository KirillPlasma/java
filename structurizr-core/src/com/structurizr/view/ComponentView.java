package com.structurizr.view;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.structurizr.model.*;

import java.util.HashSet;
import java.util.Set;

public class ComponentView extends StaticView {

    private Container container;
    private String containerId;

    ComponentView() {
    }

    ComponentView(Container container, String description) {
        super(container.getSoftwareSystem(), description);

        this.container = container;
    }

    /**
     * Gets the ID of the container associated with this view.
     *
     * @return the ID, as a String
     */
    public String getContainerId() {
        if (this.container != null) {
            return container.getId();
        } else {
            return this.containerId;
        }
    }

    void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    @JsonIgnore
    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    @Override
    public void add(SoftwareSystem softwareSystem) {
        if (softwareSystem != null && !softwareSystem.equals(getSoftwareSystem())) {
            addElement(softwareSystem, true);
        }
    }

    /**
     * Adds all containers in the software system to this view.
     */
    public void addAllContainers() {
        getSoftwareSystem().getContainers().stream()
                .forEach(this::add);
    }

    /**
     * Adds an individual container to this view.
     *
     * @param container the Container to add
     */
    public void add(Container container) {
        if (container != null && !container.equals(getContainer())) {
            addElement(container, true);
        }
    }

    /**
     * Adds all components in the container to this view.
     */
    public void addAllComponents() {
        container.getComponents().forEach(this::add);
    }

    /**
     * Adds an individual component to this view.
     *
     * @param component the Component to add
     */
    public void add(Component component) {
        if (component != null && component.getContainer().equals(getContainer())) {
            addElement(component, true);
        }
    }

    /**
     * Removes an individual container from this view.
     *
     * @param container the Container to remove
     */
    public void remove(Container container) {
        removeElement(container);
    }

    /**
     * Removes an individual component from this view.
     *
     * @param component the Component to remove
     */
    public void remove(Component component) {
        removeElement(component);
    }

    @Override
    public String getName() {
        return getSoftwareSystem().getName() + " - " + getContainer().getName() + " - Components";
    }

    @Override
    public void addAllElements() {
        addAllSoftwareSystems();
        addAllPeople();
        addAllContainers();
        addAllComponents();
    }

    @Override
    public void addNearestNeighbours(Element element) {
        super.addNearestNeighbours(element, SoftwareSystem.class);
        super.addNearestNeighbours(element, Person.class);
        super.addNearestNeighbours(element, Container.class);
        super.addNearestNeighbours(element, Component.class);
    }

    /**
     * <p>Calling this method gives you an isolated view of this {@link Container} with all its
     * component and all ingoing and outgoing relationships. Effectively, the following components
     * and relationships are added to the view:</p>
     * <ul>
     * <li>all other {@link Element}s (Person, SoftwareSystem, Container or Component) that have direct {@link Relationship}s to
     * this elements in this view including the Container itself</li>
     * <li>all other {@link Element}s (Person, SoftwareSystem, Container or Component) that are referenced by the
     * {@link Container} of this view or one of the {@link Element}s in this view</li>
     * </ul>
     * <p>{@link Relationship}s between external {@link Element}s (i.e. elements that are not part of this container) are
     * removed from the view though.
     * </p>
     * <p>Dont forget to add elements to your view prior to calling this method, e.g. by calling {@link #addAllComponents()}
     * or be selectively choosing certain components.</p>
     */
    public void addDirectDependencies() {
        final Set<Element> insideElements = new HashSet<>();
        insideElements.add(getContainer());
        getElements().stream().forEach(e -> insideElements.add(e.getElement()));

        // add relationships of all other elements to or from our inside components
        for (Relationship relationship : getContainer().getModel().getRelationships()) {
            if (insideElements.contains(relationship.getSource())) {
                addElement(relationship.getDestination());
            }
            if (insideElements.contains(relationship.getDestination())) {
                addElement(relationship.getSource());
            }
        }

        // remove all relationships between outside components, we don't care about them here
        getRelationships().stream()
                .map(RelationshipView::getRelationship)
                .filter(r -> !insideElements.contains(r.getSource()) && !insideElements.contains(r.getDestination()))
                .forEach(this::remove);
    }

    /**
     * bit ugly code here but we want to ensure that {@link #addDirectDependencies()} calls the add methods
     * directly in this class to not bypass any special handling here (i.e. don't add components of other containers)
     *
     * @param element the element to be added
     */
    private void addElement(Element element) {
        if (element instanceof Person) {
            add((Person) element);
        } else if (element instanceof SoftwareSystem) {
            add((SoftwareSystem) element);
        } else if (element instanceof Component) {
            add((Component) element);
        } else if (element instanceof Container) {
            add((Container) element);
        }
    }

}