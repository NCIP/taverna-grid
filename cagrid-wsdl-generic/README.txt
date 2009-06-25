Alex Nenadic: Merged latest changes to wsdl-activity as of 23rd June 2009.

When doing future merges, one should not that the only class where I 
have made some changes (apart from the changes in package names)
is WSDLParser.java in methods to remove references to axis 1.4 
(since cagrid-wsdl-activity depends on axis 1.2 pulled by globus):
	
	private ArrayTypeDescriptor constructMapType(TypeEntry type) 
	
	private List constructElements(List elements)
	
	private ArrayTypeDescriptor constructArrayType(TypeEntry type)
	


