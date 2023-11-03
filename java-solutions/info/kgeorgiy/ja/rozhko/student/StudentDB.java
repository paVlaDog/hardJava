package info.kgeorgiy.ja.rozhko.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.GroupQuery;
import info.kgeorgiy.java.advanced.student.Student;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements GroupQuery {
    private final Comparator<Student> comparatorByName = Comparator.comparing(Student::getLastName).thenComparing(Student::getFirstName).reversed().thenComparing(Student::getId);

    private <T> Stream<Map.Entry<T, List<Student>>> getGroupGroupingBy(Collection<Student> students, Function<Student, T> func) {
        return students.stream()
                .collect(Collectors.groupingBy(func))
                .entrySet().stream();
    }

    private List<Group> getGroupsBy(Collection<Student> students, Function<Collection<Student>, List<Student>> func) {
        return getGroupGroupingBy(students, Student::getGroup)
                .map((x) -> new Group(x.getKey(), func.apply(x.getValue())))
                .sorted(Comparator.comparing(Group::getName))
                .collect(Collectors.toList());
    }

    private GroupName getLargestGroupBy(Collection<Student> students, Comparator<Map.Entry<GroupName, Integer>> comp, Function<List<Student>, Integer> func) {
        return getGroupGroupingBy(students, Student::getGroup)
                .map(x -> Map.entry(x.getKey(), func.apply(x.getValue())))
                .max(comp).map(Map.Entry::getKey).orElse(null);
    }

    private <T> List<T> getAttributes(List<Student> students, Function<Student, T> func) {
        return students.stream()
                .map(func)
                .collect(Collectors.toList());
    }

    private List<Student> sortStudentsBy(Collection<Student> students, Comparator<Student> comparator) {
        return students.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private <T> Stream<Student> filterStudent(Collection<Student> students, T attr, Function<Student, T> func) {
        return students.stream()
                .filter(x -> func.apply(x).equals(attr));
    }

    private <T> List<Student> findStudentsBy(Collection<Student> students, T attr, Function<Student, T> func) {
        return filterStudent(students, attr, func)
                .sorted(comparatorByName)
                .collect(Collectors.toList());
    }

    /**
     * Returns student groups, where both groups and students within a group are ordered by name.
     *
     */

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroupsBy(students, this::sortStudentsByName);
    }

    /**
     * Returns student groups, where groups are ordered by name, and students within a group are ordered by id.
     *
     */
    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroupsBy(students, this::sortStudentsById);
    }

    /**
     * Returns group containing maximum number of students.
     * If there is more than one largest group, the one with the greatest name is returned.
     *
     */
    @Override
    public GroupName getLargestGroup(Collection<Student> students) {
        return getLargestGroupBy(students,
                Map.Entry.<GroupName, Integer>comparingByValue().thenComparing(Map.Entry::getKey),
                Collection::size);
    }

    /**
     * Returns group containing maximum number of students with distinct first names.
     * If there are more than one largest group, the one with the smallest name is returned.
     *
     */
    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroupBy(students,
                Map.Entry.<GroupName, Integer>comparingByValue().reversed().thenComparing(Map.Entry::getKey).reversed(),
                x -> x.stream().map(Student::getFirstName).collect(Collectors.toCollection(HashSet::new)).size());
    }

    /**
     * Returns student {@link Student#getFirstName() first names}.
     *
     */
    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getAttributes(students, Student::getFirstName);
    }

    /**
     * Returns student {@link Student#getLastName() last names}.
     *
     */
    @Override
    public List<String> getLastNames(List<Student> students) {
        return getAttributes(students, Student::getLastName);
    }

    /**
     * Returns student {@link Student#getGroup() groups}.
     *
     */
    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return getAttributes(students, Student::getGroup);
    }

    /**
     * Returns full student name.
     *
     */
    @Override
    public List<String> getFullNames(List<Student> students) {
        return getAttributes(students, x -> x.getFirstName() + " " + x.getLastName());
    }

    /**
     * Returns distinct student {@link Student#getFirstName() first names} in lexicographic order.
     *
     */
    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return new TreeSet<>(getFirstNames(students));
    }

    /**
     * Returns a {@link Student#getFirstName() first name} of the student with maximal {@link Student#getId() id}.
     *
     */
    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(Comparator.comparing(Student::getId))
                .map(Student::getFirstName).orElse("");
    }

    /**
     * Returns students ordered by {@link Student#getId() id}.
     *
     */
    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudentsBy(students, Comparator.comparing(Student::getId));
    }

    /**
     * Returns students ordered by name.
     *
     */
    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudentsBy(students, comparatorByName);
    }

    /**
     * Returns students having specified first name. Students are ordered by name.
     *
     */
    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentsBy(students, name, Student::getFirstName);
    }

    /**
     * Returns students having specified last name. Students are ordered by name.
     *
     */
    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentsBy(students, name, Student::getLastName);
    }

    /**
     * Returns students having specified groups. Students are ordered by name.
     *
     */
    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return findStudentsBy(students, group, Student::getGroup);
    }

    /**
     * Returns map of group's student last names mapped to minimal first name.
     *
     */
    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return getGroupGroupingBy(
                filterStudent(students, group, Student::getGroup).collect(Collectors.toList()), Student::getLastName)
                .map(x -> Map.entry(x.getKey(), x.getValue().stream().min(Comparator.comparing(Student::getFirstName))))
                .collect(Collectors.toMap(Map.Entry::getKey, x -> x.getValue().map(Student::getFirstName).orElse("")));
    }
}
