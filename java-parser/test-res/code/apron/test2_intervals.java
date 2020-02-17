{
	int i, j, k;
	i = -1;
	assert i == -1;
	j = random;
	if(j < 0)
		k = i*j;
	else k = j;
	assert k >=0;
	assert k <=100;//Warn
	assert i == -1;
	assert i > -1; //Warn
	if(k < 123) {
		assert k < 123;
		assert k != 123;//Warn
	}
}
