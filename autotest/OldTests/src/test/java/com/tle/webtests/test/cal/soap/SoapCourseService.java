package com.tle.webtests.test.cal.soap;

/** */
public interface SoapCourseService {
  /**
   * Get the XML for a course.
   *
   * @param courseCode The Code of the course.
   * @return The XStreamed XML of the {@code CourseInfo}.
   */
  String getCourse(String courseCode);

  /**
   * Add a course.
   *
   * @param courseXml The XStreamed XML of the {@code CourseInfo}. For an example of the XML format,
   *     use getCourse
   */
  void addCourse(String courseXml);

  /**
   * @param courseXml The edited course XML in XStream format. Use getCourse to get an existing
   *     course's XML
   */
  void editCourse(String courseXml);

  /**
   * Upload many courses at once using a CSV file.
   *
   * @param csvText The contents of a CSV file in the format: <br>
   *     <table>
   * 	<th>
   * 	 <td>"Name"</td><td>"Description"</td><td>"Code"</td><td>"Citation"</td><td>"Start"</td>
   *   <td>"End"</td><td>"Students"</td><td>"Type"</td><td>"DepartmentName"</td>
   *  </th>
   *  <tr>
   *   <td>Course1</td><td>Course1 Description</td><td>C001</td><td>Harvard</td><td>31/01/2009</td>
   *   <td>20/12/2009</td><td>100</td><td>i</td><td>My Deparment</td>
   *  </tr>
   * </table>
   *     <br>
   *     Note:
   *     <ul>
   *       <li>Type can be i (internal), e (external), s (staff)
   *       <li>Dates must be in format dd/MM/yyyy
   *     </ul>
   */
  void bulkImport(String csvText);

  /**
   * @return A list of course Codes
   */
  String[] enumerateCourseCodes();

  /**
   * Removes a course from the database. Note that the course cannot be reference anywhere else in
   * the system. (E.g. by copyright activations or hierarchy topics)
   *
   * @param courseCode The Code of the course to delete
   */
  void delete(String courseCode);
}
