/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.entity.services;

/**
 * The WSDL file for this service is available at
 * http://INSTITUTION_URL/services/calcourses.service?wsdl (for compatibility
 * reasons)
 */
public interface SoapCourseService
{
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
	 * @param courseXml The XStreamed XML of the {@code CourseInfo}. For an
	 *            example of the XML format, use getCourse
	 */
	void addCourse(String courseXml);

	/**
	 * @param courseXml The edited course XML in XStream format. Use getCourse
	 *            to get an existing course's XML
	 */
	void editCourse(String courseXml);

	/**
	 * Upload many courses at once using a CSV file.
	 * 
	 * @param csvText The contents of a CSV file in the format: <br>
	 *            <table>
	 *            <th>
	 *            <td>"Name"</td>
	 *            <td>"Description"</td>
	 *            <td>"Code"</td>
	 *            <td>"Citation"</td>
	 *            <td>"Start"</td>
	 *            <td>"End"</td>
	 *            <td>"Students"</td>
	 *            <td>"Type"</td>
	 *            <td>"DepartmentName"</td></th>
	 *            <tr>
	 *            <td>Course1</td>
	 *            <td>Course1 Description</td>
	 *            <td>C001</td>
	 *            <td>Harvard</td>
	 *            <td>31/01/2009</td>
	 *            <td>20/12/2009</td>
	 *            <td>100</td>
	 *            <td>i</td>
	 *            <td>My Deparment</td>
	 *            </tr>
	 *            </table>
	 * <br>
	 *            Note:
	 *            <ul>
	 *            <li>Type can be i (internal), e (external), s (staff)</li>
	 *            <li>Dates must be in format dd/MM/yyyy</li>
	 *            </ul>
	 */
	void bulkImport(String csvText);

	/**
	 * @return A list of course Codes
	 */
	String[] enumerateCourseCodes();

	/**
	 * Removes a course from the database. Note that the course cannot be
	 * reference anywhere else in the system. (E.g. by copyright activations or
	 * hierarchy topics)
	 * 
	 * @param courseCode The Code of the course to delete
	 */
	void delete(String courseCode);

	/**
	 * Archives a course so that is not selectable in the web-ui (can still be
	 * edited in the Administration console)
	 * 
	 * @param courseCode The Code of the course to archive
	 */
	void archiveCourse(String courseCode);

	/**
	 * Un-archives a course so that is selectable in the web-ui
	 * 
	 * @param courseCode The Code of the course to un-archive
	 */
	void unarchiveCourse(String courseCode);
}
