{
  var query;
  query = "SELECT $ || (RETAIL/100) FROM INVENTORY WHERE";
  if(l != null)
    query = query + "WHOLESALE > " + l + " AND ";

  var per;
  per = "SELECT TYPECODE, TYPEDESC FROM TYPES WHERE NAME = 'fish' OR NAME = 'meat'";
  query = query + "TYPE IN (" + per + ");";
  return query;  
}
